package com.umdcs4995.whiteboard.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.umdcs4995.whiteboard.Globals;
import com.umdcs4995.whiteboard.protocol.WbProtocolException;
import com.umdcs4995.whiteboard.protocol.WhiteboardProtocol;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * The SocketService maintains a background connection to the server.  It's methods are used by
 * the protocols to send and receive messages from the server.
 * Created by rob on 2/27/16.
 */
public class SocketService extends Service {
    private final String TAG = "SocketService";
    private String TCP_ADDRESS = null;

    //Create binder to bind service with client
    private final IBinder ibinder = new SocketLocalBinder();

    private WhiteboardProtocol protocol;

    //Socket to do the connection.
    private Socket socket;

    // Poor man's enum for Message id's
    public class Messages {
        public static final String CREATE_WHITEBOARD = "createWhiteboard";
        public static final String JOIN_WHITEBOARD = "joinWhiteboard";

        public static final String CHAT_MESSAGE = "chat message";
        public static final String DRAW_EVENT = "drawevent";
        public static final String ME = "me";
        public static final String DELETE_WHITEBOARD = "deleteWhiteboard";

        public static final String MOTION_EVENT = "motionevent";


        // TODO: put the rest of the messages in here
    }

    /**
     * Constructor for socket service.  Difficult to say when this method is (if ever) called.
     * Better to create and init() method and call it in the activity once the service is bound.
     */
    public SocketService() {
        //This method should never be called directly.
    }


    /**
     * This method is called when the service is started.  Note that the service should be
     * responsible for starting connecting the socket here and disconnecting in onDestroy.
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            TCP_ADDRESS = Globals.getInstance().getServerAddress();
            connect();

            // TODO: Move listener code to other parts of the codebase
            setupListeners();

            return super.onStartCommand(intent, flags, startId);
        } catch (Exception e) {
            e.printStackTrace();
            onDestroy();
        }

        return -1;
    }

    /**
     * Set the protocol accordingly.
     * @param wp
     */
    public void setProtocol(WhiteboardProtocol wp) {
        protocol = wp;
    }

    /**
     * Important to disconnect the socket to avoid memory leaks.
     */
    @Override
    public void onDestroy() {
        socket.off();
        socket.disconnect();
        Log.i(TAG, "Whiteboard Disconnected");
        super.onDestroy();
    }

    /**
     * Adds a listener (essentially an anonymous function) for a specific message
     * @param id
     * @param listener
     */
    public void addListener(String id, Emitter.Listener listener) {
        socket.on(id, listener);
    }

    /**
     * Removes the message listener when the socket is closed.
     * @param id
     */
    public void clearListener(String id) {
        socket.off(id);
    }

    /**
     * Setup the listeners.  These will process incoming messages on the sockets.
     */
    private void setupListeners() {
        //First listener for chat messages.
        addListener(Messages.DRAW_EVENT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                //On a chat message, simply log it.  Note that all messages at this time
                //come in as chat messages.
                try {
                    Log.v(TAG, "INCOMING: " + (String) args[0]);
                } catch (Exception e) {
                    Log.e(TAG, "Error in call method of chat message listener");
                }

                //After logging, process the message using the protocol.
                try {
                    JSONArray parsed = new JSONArray((String)args[0]);
                    protocol.inc(parsed);
                } catch (NullPointerException e) {
                    Log.e(TAG, "NullpointerError Error, malformed string");
                } catch (JSONException e) {
                    Log.e(TAG, "Problem parsing JSON response from server");
                }
            }
        });
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return ibinder;
    }

    /**
     * Attempts to open the socket.
     */
    private void connect() {
        try {
            socket = IO.socket(TCP_ADDRESS);
            socket.connect();
            Log.i(TAG, "Socket Opened");
        } catch(URISyntaxException e) {
            Log.e(TAG, "Malformed connection address");
        }
    }

    /**
     * Sends an outgoing message (JSON Object) to the server. JSON Object
     * will have toString() called on it.
     * @param id        String that contains the key for the server function
     * @param message   JSON Object that is passed in with message data.
     */
    public void sendMessage(String id, JSONObject message) throws ConnectivityException {
        if(!Globals.getInstance().isConnectedToServer()) {
            throw new ConnectivityException(ConnectivityException.TYPE_NODEVICECONNECTIVITY,
                    "Attempted to send a message without an internet connection: " +
                            message.toString());
        }
        sendMessage(id, message.toString());
    }
    public void sendMessage(String id, JSONArray message) throws ConnectivityException {
        if(!Globals.getInstance().isConnectedToServer()) {
            throw new ConnectivityException(ConnectivityException.TYPE_NODEVICECONNECTIVITY,
                    "Attempted to send a message without an internet connection: " +
                            message.toString());
        }
        sendMessage(id, message.toString());
    }

    /**
     * Sends an outgoing message to the server.
     * @param id        String that contains the key for the server function
     * @param message   String that contains the message data.
     */
    public void sendMessage(String id, String message) throws ConnectivityException {
        if(!Globals.getInstance().isConnectedToServer()) {
            throw(new ConnectivityException(ConnectivityException.TYPE_NODEVICECONNECTIVITY,
                    "Attempted to send a message without an internet connection: " + message));
        }
        Log.v(TAG, "SENT: " + id + ": " + message);
        socket.emit(id, message);
    }

    /**
     * This class returns a bound reference to the socket.  It acts as an intermediary between
     * the
     */
    public class SocketLocalBinder extends Binder {
        public SocketService getService() {
            return SocketService.this;
        }
    }


    //Instance of a runnable used to reconnect the client if connection is lost.
    private ReconnectRunnable reconnectRunnable = new ReconnectRunnable();
    /**
     * Method is called to start reconnection.
     * @return
     */
    public synchronized void startReconnecting() {
        Log.i("Globals", "StartReconnecting");
        if(!currentlyReconnecting) {
            currentlyReconnecting = true;
            reconnectRunnable.reset();
            Thread t = new Thread(reconnectRunnable);
            t.start();
        }
    }

    boolean currentlyReconnecting = false;
    /**
     * Sets the connecting flag for the service.  This is needed to make sure the service
     * doesn't launch more than one reconnecting thread.
     * @param reconnecting
     */
    public void setCurrentlyReconnecting(boolean reconnecting) {
        currentlyReconnecting = reconnecting;
    }
}