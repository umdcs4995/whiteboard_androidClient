package com.umdcs4995.whiteboard;

/**
 * Class containing app-wide constants.  Currently used for permissions.
 * Created by Rob on 2/2/2016.
 */
public class AppConstants {
    /**
     * Integer for the max number of repainted lines that should be allowed.  This is roughly equiv
     * to the number of threads able to be opened by the app, but race conditions may be possible.
     * Increase with caution.
     */
    public static final int MAX_REPAINT_COUNT = 10;

    public static final int PERMISSION_CAMERA = 0;
    public static final int DEViCE_STORAGE = 1;


    //Below are possible broadcast intent request / receiver strings.
    public static String BM_REPAINT = "repaintRequest";
    public static String BM_RECONNECTED = "reconnected";
    public static String BM_LINEPAINTED = "linePainted";
}
