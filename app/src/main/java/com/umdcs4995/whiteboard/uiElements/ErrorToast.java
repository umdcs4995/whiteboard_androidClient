package com.umdcs4995.whiteboard.uiElements;

import android.content.Context;
import android.widget.Toast;

/**
 * Class allows for quick and dirty error displays in the form of a toast.
 * Created by Rob on 2/2/2016.
 */
public class ErrorToast {
    /**
     * Constructor for a toast with no extra content.
     * @param context Takes in the context for which to call the toast.  To use this, pass in
     *                this.getApplicationContext() in almost all cases.
     */
    public ErrorToast(Context context) {
        Toast toast = Toast.makeText(context, "Error", Toast.LENGTH_SHORT);
        toast.show();
    }

    /**
     * Constructor for a toast with an extra text values.
     * @param context Takes in the context for which to call the toast.  To use this, pass in
     *                this.getApplicationContext() in almost all cases.
     * @param extraText An extra string to be appended onto the end of the toast.  Usually
     *                  something simple like "Settings menu item pressed."
     */
    public ErrorToast(Context context, String extraText) {
        Toast toast = Toast.makeText(context, "Error: " + extraText,
                Toast.LENGTH_SHORT);
        toast.show();
    }
}
