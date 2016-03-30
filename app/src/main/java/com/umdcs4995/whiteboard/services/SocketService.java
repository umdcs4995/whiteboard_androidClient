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

import org.json.JSONObject;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * Created by rob on 2/27/16.
 */
public class SocketService extends Service {
    private final String TAG = "SocketService";
    private final String TCP_ADDRESS = Globals.getInstance().getServerAddress();

    //Create binder to bind service with client
    private final IBinder ibinder = new SocketLocalBinder();

    private WhiteboardProtocol protocol;

    //Socket to do the connection.
    private Socket socket;

    // Poor man's enum for Message id's
    public class Messages {
        public static final String CREATE_WHITEBOARD = "createWhiteboard";
        public static final String JOIN_WHITEBOARD = "joinWhiteboard";

        public static final String MOTION_EVENT = "motionevent";

        // Uncomment the following 2 lines when the server has been fixed:
        //public static final String CHAT_MESSAGE = "chat message";
        //public static final String DRAW_EVENT = "drawevent";

        // The following 2 lines are a workaround while the server is being fixed:
        public static final String CHAT_MESSAGE = "drawevent";
        public static final String DRAW_EVENT = "chat message";

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
        connect();

        // TODO: Move listener code to other parts of the codebase
        setupListeners();

        return super.onStartCommand(intent, flags, startId);
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
                    protocol.inc((String) args[0]);
                } catch (WbProtocolException e) {
                    Log.e(TAG, "Protocol Error, malformed string");
                } catch (NullPointerException e) {
                    Log.e(TAG, "NullpointerError Error, malfromed string");
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
     * Used to convert JSONObject to String before calling sendMessage
     * @param id
     * @param message
     */
    public void sendMessage(String id, JSONObject message) {
        sendMessage(id, message.toString());
    }

    /**
     * Sends an outgoing message to the server.
     */
    public void sendMessage(String id, String message){
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
}