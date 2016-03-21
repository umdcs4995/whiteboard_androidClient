package com.umdcs4995.whiteboard.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.umdcs4995.whiteboard.protocol.WbProtocolException;
import com.umdcs4995.whiteboard.protocol.WhiteboardProtocol;

import org.json.JSONException;
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
    private final String TCPADDRESS = "https://lempo.d.umn.edu:4995";

    //Create binder to bind service with client
    private final IBinder ibinder = new SocketLocalBinder();

    private WhiteboardProtocol protocol;

    //Socket to do the connection.
    private Socket socket;

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
        //Test the server
        setupListeners();
        this.sendMessage(null, "Hello World");
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * Set the protocol
     */
    public void setProtocol(WhiteboardProtocol wp) {
        protocol = wp;
    }

    /**
     * Important to disconnect the socket to avoid memory leaks.
     */
    @Override
    public void onDestroy() {
        //This hopefully stops socket memory leaks.
        socket.disconnect();
        Log.i(TAG, "Whiteboard Disconnected");
        super.onDestroy();
    }

    /**
     * Setup the listeners.  These will process incoming messages on the sockets.
     */
    private void setupListeners() {
        //First listener for chat messages.
        socket.on("chat message", new Emitter.Listener() {
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
                    Log.e(TAG, "Protocol Error, malfromed string");
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
            socket = IO.socket(TCPADDRESS);
            socket.connect();
            Log.i(TAG, "Socket Opened");
        } catch(URISyntaxException e) {
            Log.e(TAG, "Malfromed connection address");
        }
    }

    /**
     * Sends an outgoing message to the server.
     */
    public void sendMessage(String id, String message){
        //Temporary code to get the server to respond.
        id = "chat message";
        //end temporary code.
        Log.v(TAG, "SENT: " + message);
        socket.emit(id, message);

        // Test to see if I can send back to the server
        try {
            JSONObject jsonData = new JSONObject();
            jsonData.put("name", "testBoard"); // Set the first name/pair
            jsonData.put("access", "public");

            socket.emit("createWhiteboard", jsonData.toString());
        }
        catch(JSONException ex) {
            ex.printStackTrace();
        }

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