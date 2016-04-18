package com.umdcs4995.whiteboard;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.support.v7.util.SortedList;
import android.util.Log;
import com.umdcs4995.whiteboard.drawing.DrawingEventQueue;
import com.umdcs4995.whiteboard.protocol.WhiteboardProtocol;
import com.umdcs4995.whiteboard.services.SocketService;
import com.umdcs4995.whiteboard.whiteboarddata.Whiteboard;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.net.URLConnection;
import java.util.Comparator;
import java.util.Date;
import java.util.PriorityQueue;
import java.util.concurrent.LinkedBlockingQueue;

import io.socket.emitter.Emitter;


/**
 * Singleton class created for holding objects global to the application.
 * Created by rob on 2/26/16.
 */
public class Globals {

    //Instance for the global variables.
    private static Globals instance = null;

    private Context context;

    //The drawing event queue for the current whiteboard.
    private DrawingEventQueue drawingEventQueue;
    private WhiteboardProtocol whiteboardProtocol;

    private String currentWhiteboard = "";

    //Socket service stuff
    private boolean socketServiceRunning = false;
    Intent socketServiceIntent;
    private SocketService socketService;
    private boolean isSocketServiceBound = false;

    //Instance of Whiteboard.
    private Whiteboard whiteboard;

    /**
     * Private data member which binds the xmpp service to the client.
     */
    private ServiceConnection socketConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            SocketService.SocketLocalBinder binder = (SocketService.SocketLocalBinder) service;
            socketService = binder.getService();
            isSocketServiceBound = true;

            //At this point the service is bound.
            socketService.setProtocol(whiteboardProtocol);
            whiteboardProtocol.setSocketService(socketService);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isSocketServiceBound = false;
        }
    };


    //Hidden constructor.
    private Globals(Context applicationContext) {
        //Setup the priortiy measurements for the queue.
        this.context = applicationContext;
        serverAddress = getServerAddress();
        socketServiceIntent = new Intent(context, SocketService.class);
        whiteboardProtocol = new WhiteboardProtocol();
    }

    /**
     * Called to initiate the singleton instance.  Note this needs to be called prior to the
     * getInstance method otherwise the app will blow up.
     * @return
     */
    public static void init(Context applicationContext) {
        if(instance == null) {
            instance = new Globals(applicationContext);
        }
    }

    /**
     * Returns the singleton instance.
     * @return an instance of Globals.
     */
    public static Globals getInstance(){
        return instance;
    }

    /**
     * Get the Whiteboard draw event queue.
     * @return
     */
    public DrawingEventQueue getDrawEventQueue(){
        return this.drawingEventQueue;
    }

    /**
     * Sets the DrawingEventQueue
     */
    public void setDrawEventQueue(DrawingEventQueue deq) {
        drawingEventQueue = deq;
    }

    /**
     * This method starts the socket service.
     */
    public void startSocketService() {
        if(socketServiceRunning) {
            //Safest thing to do here is to restart it.
            stopSocketService();
            startSocketService();
        } else {
            context.startService(socketServiceIntent);
            context.bindService(socketServiceIntent, socketConnection, Context.BIND_AUTO_CREATE);
            socketServiceRunning = true;
        }
    }

    /**
     * Stops (and destroys) the socket service.
     */
    public void stopSocketService() {
        if(socketServiceRunning) {
            context.stopService(socketServiceIntent);
            context.unbindService(socketConnection);
            socketServiceRunning = false;
        }
    }

    /**
     * Returns a reference to the socket service.
     */
    public SocketService getSocketService() {
        return socketService;
    }

    /**
     * Gets the application context
     */
    public Context getGlobalContext() {
        return context;
    }

    /**
     * Gets the Whiteboard protocol.
     * @return the Whiteboard protocol
     */
    public WhiteboardProtocol getWhiteboardProtocol() {
        return whiteboardProtocol;
    }

    private static String serverAddress = "";

    /**
     * Builds the server address based on the data in strings.xml
     * Uses private field serverAddress to load it once and not again
     */
    public String getServerAddress() {
        if(serverAddress == "" && context != null) {
            String protocol = (context.getResources().getBoolean(R.bool.secure) ? "https" : "http") + "://";
            String port = context.getResources().getBoolean(R.bool.secure) ? context.getString(R.string.secure_port) : context.getString(R.string.port);
            serverAddress = protocol + context.getString(R.string.hostname) + ":" + port;
        }
        return serverAddress;
    }

    public void refreshCurrentWhiteboard() {
        socketService.addListener(SocketService.Messages.ME, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject recvd = (JSONObject)args[0];
                try {
                    currentWhiteboard = recvd.getString("whiteboard");
                    Log.i("meMessage", "my whiteboard is: " + currentWhiteboard);
                } catch (JSONException e) {
                    Log.w("meMessage", "You are not in a whiteboard (or there was another problem)");
                }
                socketService.clearListener(SocketService.Messages.ME);
            }
        });
        try {
            socketService.sendMessage(SocketService.Messages.ME, "");
        } catch (Exception e) {
            Log.e("Globals", "Not connected to server");
        }
    }

    public String getCurrentWhiteboard() {
        return currentWhiteboard;
    }

    /**
     * Returns the current whiteboard instance.
     * @return
     */
    public Whiteboard getWhiteboard() {
        return whiteboard;
    }

    /**
     * Sets the active instance of a Whiteboard to a different Whiteboard.
     * @param wb
     */
    public void setWhiteboard(Whiteboard wb) {
        whiteboard = wb;
    }

    public boolean isConnectedToInternet() {

        if(context == null)
            return false;

        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        return isConnected;
    }

    private long lastSuccessfulUpdate = 0;
    private boolean lastCheckSuccessful = false;
    // check server connectivity every 60 seconds
    private static long checkInterval = 1000 * 60;

    public boolean isConnectedToServer() {
        if(!isConnectedToInternet())
            return false;

        // if it has not been enough time, don't check again
        long currentTime = new Date().getTime();
        if(lastCheckSuccessful && currentTime < lastSuccessfulUpdate + checkInterval)
            return true;

        try{
            Log.i("Globals", "Checking server connectivity");
            URL server = new URL(getServerAddress());
            URLConnection connection = server.openConnection();
            //TODO: this might not be long enough...
            connection.setConnectTimeout(1);
            connection.connect();
            lastSuccessfulUpdate = new Date().getTime();
            lastCheckSuccessful = true;
        } catch (Exception e) {
            lastCheckSuccessful = false;
        }

        return lastCheckSuccessful;
    }

}
