package com.umdcs4995.whiteboard.protocol;

import android.util.Log;

/**
 * Class is designed to handle errors in the protocol without causing the app to blow up.
 * Created by rob on 2/27/16.
 */
public class WbProtocolException extends Exception {

    private Exception parent;
    private String message;

    public WbProtocolException(String message, Exception parent) {
        this.parent = parent;
        this.message = message;

        Log.e("WbProtocol", getMessage());
    }

    public String getMessage() {
        StringBuilder builder = new StringBuilder();

        builder.append(message);

        if(parent != null) {

            builder.append("\n\n");
            builder.append(parent.getMessage());

        }

        return builder.toString();
    }

}