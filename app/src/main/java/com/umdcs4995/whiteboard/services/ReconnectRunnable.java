package com.umdcs4995.whiteboard.services;

import android.util.Log;

import com.umdcs4995.whiteboard.Globals;

/**
 * This runnable class is designed to try to check for a server connection.  It should be called
 * after the device loses activity and should self-terminate.
 * Created by Rob on 4/20/2016.
 */
public class ReconnectRunnable implements Runnable {
    private final String TAG = "ReconnectRunnable";

    //flag used for debugging
    private boolean debugging = true;

    //flag to check for connection.
    private boolean connected = false;

    //the default delay time for the first attempt to reconnect.
    private int delay = 2000;

    //the number of attempts the client has made to reconnect.
    private int attempts = 1;

    /**
     * Here, we wait a specified delay time.
     */
    @Override
    public void run() {
        while(!connected) {
            try {
                Thread.sleep(delay); //Wait the specified time

                //Log it
                Log.v(TAG, "Reconnect Attempt #" + attempts);

                //Check for connection.
                if(Globals.getInstance().isConnectedToServer()) {
                    //Connection reestablished.
                    connectionEstablished();
                    onReconnect();
                } else {
                    //Connection not established, increments the attempts.
                    attempts++;
                }
            } catch(InterruptedException ie ) {
                //Do nothing here.
            }
        }
    }


    /**
     * Method is called by run() when the connection has been established.  This should set the
     * connection flag and the run method will terminate the loop.
     */
    private void connectionEstablished() {
        Log.v(TAG, "Connection reestablished");
        connected = true;
    }

    /**
     * Method is called up reconnection.
     */
    private void onReconnect() {
        
    }
}
