package com.umdcs4995.whiteboard.services;

import com.umdcs4995.whiteboard.Globals;

/**
 * Special exception thrown by the socket service when there was an error communicating with the
 * server.  These exceptions can then be handled by the application to
 * Created by Rob on 4/19/2016.
 */
public class ConnectivityException extends Exception {
    /**
     * Used when the device has no Internet connection.
     */
    public static int TYPE_NODEVICECONNECTIVITY = 2;


    private int errorCode;
    private String message;

    /**
     * @param errorCode See static data members for possible error types.
     * @param message Message to display for logging information.
     */
    public ConnectivityException(int errorCode, String message) {
        this.message = message;
        this.errorCode = errorCode;

        //Attempt to reconnect to the server
        Globals.getInstance().getSocketService().startReconnecting();
    }

    // ======= Getters and Setters
    public String getMessage() {
        return message;
    }
}
