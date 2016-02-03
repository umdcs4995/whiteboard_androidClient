package com.umdcs4995.whiteboard;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import java.io.IOException;

import uiFragments.ErrorToast;

/**
 * This class will handle interfacing the camera with the app.
 * Created by Rob on 2/2/2016.
 */
public class CameraWb extends SurfaceView implements SurfaceHolder.Callback {

    //Private data members
    private Context context; //Contains a reference to the current application.  Set in constructor.
    private boolean cameraAvailable; //Flag to see if camera is available.
    private Camera camera; //Instance of the camera.
    private SurfaceHolder surfaceHolder; //This instan

    /**
     * Default constructor for the Camera class.  Please pass in a call to getApplicationContext()
     * method.
     * @param context
     */
    public CameraWb(Context context) {
        super(context); //Call the surface view to place the context.
        this.context = context;
        this.cameraAvailable = checkCameraAvailability(context);
        if(cameraAvailable) {
            if(Camera.getNumberOfCameras() > 1) camera = Camera.open(1);
            else camera = Camera.open();
        } else {
            camera = null;
        }

        //========= I Dont Know What This Does Exactly, but it works ======
        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);


    }


    /** Check if this device has a camera */
    private boolean checkCameraAvailability(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }


    //================ SURFACE METHODS ============================
    //These methods handle the view (the UI element) and handle how to display the camera.

    public void surfaceCreated(SurfaceHolder holder) {
        if(!cameraAvailable || camera == null) return; //Camera isn't working / on this device, so bail.

        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            camera.setPreviewDisplay(holder);
            camera.startPreview();
        } catch (Exception e) {
            new ErrorToast(context, "Exception in CameraWB::surfaceCreated(..)");
        }
    }


    public void surfaceDestroyed(SurfaceHolder holder) {
        // Called when the activity is destroyed.
    }


    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        if(!cameraAvailable || camera == null) return; //Camera isn't working / on this device, so bail.

        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        if (surfaceHolder.getSurface() == null){
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            camera.stopPreview();
        } catch (Exception e){
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here
        Camera.Parameters parameters = camera.getParameters();
        Display display = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

        if(display.getRotation() == Surface.ROTATION_0)
        {
            parameters.setPreviewSize(h, w);
            camera.setDisplayOrientation(90);
        }

        if(display.getRotation() == Surface.ROTATION_90)
        {
            parameters.setPreviewSize(w, h);
        }

        if(display.getRotation() == Surface.ROTATION_180)
        {
            parameters.setPreviewSize(h, w);
        }

        if(display.getRotation() == Surface.ROTATION_270)
        {
            parameters.setPreviewSize(w, h);
            camera.setDisplayOrientation(180);
        }

        // start preview with new settings
        try {
            //Rotate the camera appropriately.
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();

        } catch (Exception e){
            new ErrorToast(context, "Exception in CameraWb::surfaceChanged(..)");
        }
    }

    //Method here rotates the camera based on the rotation of the screen.
    public void setCameraOritentation(int degrees) {
        if(camera != null) camera.setDisplayOrientation(degrees);
        else return;
    }
}
