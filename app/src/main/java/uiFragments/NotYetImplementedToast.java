package uiFragments;

import android.content.Context;
import android.widget.Toast;

/**
 * This class creates a Toast that displays "Not Yet Implemented" on the screen.
 * Created by Rob on 2/1/2016.
 */
public class NotYetImplementedToast {

    /**
     * Constructor for a toast with no extra content.
     * @param context Takes in the context for which to call the toast.  To use this, pass in
     *                this.getApplicationContext() in almost all cases.
     */
    public NotYetImplementedToast(Context context) {
        Toast toast = Toast.makeText(context, "Not Yet Implemented", Toast.LENGTH_SHORT);
        toast.show();
    }

    /**
     * Constructor for a toast with an extra text values.
     * @param context Takes in the context for which to call the toast.  To use this, pass in
     *                this.getApplicationContext() in almost all cases.
     * @param extraText An extra string to be appended onto the end of the toast.  Usually
     *                  something simple like "Settings menu item pressed."
     */
    public NotYetImplementedToast(Context context, String extraText) {
        Toast toast = Toast.makeText(context, "Not Yet Implemented. " + extraText,
                Toast.LENGTH_SHORT);
        toast.show();
    }

}
