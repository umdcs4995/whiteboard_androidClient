package com.umdcs4995.whiteboard;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.widget.FrameLayout;

import uiFragments.NotYetImplementedToast;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class MasterWhiteboardAddActivity extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_master_whiteboard_add);

        //Create the toolbar instance here.  The toolbar is the thing at the top with the name
        //and overflow menu.  Sets the title.
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//        if(getSupportActionBar() != null) { //Set the title.
//            getSupportActionBar().setTitle("Whiteboard");
//        }


        //Floating Action button stuff listed below.
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.addFab);
        fab.setOnClickListener(new View.OnClickListener() {
            //Set the listener for when the button is pressed
            @Override
            public void onClick(View view) {
                //Done with the activity, so go back.
                finish();
            }
        });

        //Camera stuff below.


        //Check for camera permission
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA);

        if(permissionCheck != PackageManager.PERMISSION_GRANTED) {
            //Request camera access.
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    AppConstants.PERMISSION_CAMERA);
        } else {
            //Request already granted, so just do the camera stuff.
            goGoCamera();
        }

    }


    /**
     * This method is called when the user clicks "Grant" or "Deny" for any permission.
     * For this activity, that should only be a camera request.  None-the-less, to extend this
     * code, set a constant in the AppConstants class and then add another if statement for
     * the new request code.  This is only neccessary in Android 6.0+
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {

        if(requestCode == AppConstants.PERMISSION_CAMERA) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                goGoCamera();

            } else {
                //Lets do nothing, the camera permission was denied.
            }
        }

    }


    /**
     * This method sets up the camera window.
     */
    private void goGoCamera() {
//        int degrees = 0;
//        int rotation = this.getWindowManager().getDefaultDisplay().getRotation();
//
//        switch (rotation) {
//            case Surface.ROTATION_0: degrees = 0; break;
//            case Surface.ROTATION_90: degrees = 90; break;
//            case Surface.ROTATION_180: degrees = 180; break;
//            case Surface.ROTATION_270: degrees = 270; break;
//        }

        CameraWb cameraWb = new CameraWb(getApplicationContext());
//        cameraWb.setCameraOritentation(degrees);
        FrameLayout cameraWindow = (FrameLayout) findViewById(R.id.camera_window);
        cameraWindow.addView(cameraWb);
    }

}
