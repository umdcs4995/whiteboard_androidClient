package com.umdcs4995.whiteboard.services;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.umdcs4995.whiteboard.AppConstants;
import com.umdcs4995.whiteboard.Globals;

/**
 * This runnable class is designed to try to check for a server connection.  It should be called
 * after the device loses activity and should self-terminate.
 * Created by Rob on 4/20/2016.
 */
public class ReconnectRunnable implements Runnable {
    private final String TAG = "ReconnectRunnable";
    private final int OPENDELAY = 2000;
    private final int MAXATTEMPTS = 6;

    //flag to check for connection.
    private boolean connected = false;

    //the default delay time for the first attempt to reconnect.
    private int delay = OPENDELAY;

    //the number of attempts the client has made to reconnect.
    private int attempts = 1;

    /**
     * Here, we wait a specified delay time.
     */
    @Override
    public void run() {
        while(!connected && attempts <= MAXATTEMPTS) {

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
                    if(attempts == MAXATTEMPTS) {
                        //Here, we call onFailure.
                        onFailure();
                    }
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
        Globals.getInstance().getSocketService().setCurrentlyReconnecting(false);
        Log.i(TAG, "Broadcasting reconnection message.");
        Intent intent = new Intent(AppConstants.BM_RECONNECTED);
        LocalBroadcastManager.getInstance(Globals.getInstance().getGlobalContext()).sendBroadcast(intent);
    }

    /**
     * Method called if the max number of attempts is reached.
     */
    private void onFailure() {
        Globals.getInstance().getSocketService().setCurrentlyReconnecting(false);
        Log.i(TAG, "Broadcasting failure to reconnect message.");
        Intent intent = new Intent(AppConstants.BM_CONNECTIONLOST);
        LocalBroadcastManager.getInstance(Globals.getInstance().getGlobalContext()).sendBroadcast(intent);
    }

    /**
     * Method called to reset the ReconnectRunnable
     */
    public void reset() {
        connected = false;
        delay = OPENDELAY;
        attempts = 1;
    }


}
