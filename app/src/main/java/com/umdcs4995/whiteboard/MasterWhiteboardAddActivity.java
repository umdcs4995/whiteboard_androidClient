package com.umdcs4995.whiteboard;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * Also contains a drawing canvas.
 */
public class MasterWhiteboardAddActivity extends AppCompatActivity implements View.OnClickListener,
        GestureDetector.OnGestureListener,
        GestureDetector.OnDoubleTapListener {

    private DrawingView drawView;
    private ImageButton currPaint, drawBtn, eraseBtn, newBtn, saveBtn, fileBtn, loadBtn, driveBtn;


    // Test button that will load an image from a url
    private static final String TAG = MasterWhiteboardAddActivity.class.getSimpleName();
    //Test link to a connect the dots. good measure for it "working" would be to be
    //able to accurately draw over the dots.
    private String testURL = "http://www.connectthedots101.com/dot_to_dots_for_kids/Pachycephalosaurus/Pachycephalosaurus_with_Patches_connect_dots.png";


    //brush sizes
    private float smallBrush, mediumBrush, largeBrush;

    //option menus for the buttons and paints
    private LinearLayout masterOptionButtons, masterPaintOptions;

    //gesture detector for swipe menus
    private GestureDetectorCompat masterDetector;

    //background view
    private RelativeLayout background;

    //Camera Window
    private FrameLayout cameraWindow;

    /**
     * Creates the floating action button, the drawing board, and initializes
     * the draw, erase, save, and new buttons. Also chooses the sizes for the paint brushes.
     * @param savedInstanceState used for the super onCreate()
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_master_whiteboard_add);

        // Instantiate the gesture detector with the
        // application context and an implementation of
        // GestureDetector.OnGestureListener
        masterDetector = new GestureDetectorCompat(getApplicationContext(),this);
        // Set the gesture detector as the double tap
        // listener.
        masterDetector.setOnDoubleTapListener(this);

        background = (RelativeLayout)findViewById(R.id.background);

        //Frames to hold the buttons
        masterOptionButtons = (LinearLayout) findViewById(R.id.optionButtons);
        masterPaintOptions = (LinearLayout) findViewById(R.id.paint_colors);

        //Drawing view and Buttons
        drawView = (DrawingView)findViewById(R.id.drawing);
        LinearLayout paintLayout = (LinearLayout)findViewById(R.id.paint_colors);

        currPaint = (ImageButton)paintLayout.getChildAt(0);
        currPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));

        drawBtn = (ImageButton)findViewById(R.id.draw_btn);
        drawBtn.setOnClickListener(this);

        fileBtn = (ImageButton)findViewById(R.id.addFile);
        fileBtn.setOnClickListener(this);

        eraseBtn = (ImageButton)findViewById(R.id.erase_btn);
        eraseBtn.setOnClickListener(this);

        newBtn = (ImageButton)findViewById(R.id.new_btn);
        newBtn.setOnClickListener(this);

        saveBtn = (ImageButton)findViewById(R.id.save_btn);
        saveBtn.setOnClickListener(this);

        loadBtn = (ImageButton)findViewById(R.id.load);
        loadBtn.setOnClickListener(this);

        driveBtn = (ImageButton)findViewById(R.id.drive_save);
        driveBtn.setOnClickListener(this);

        //set the size of the brushes
            //small = 5dp
            //medium = 10dp
            //large = 15dp
        smallBrush = getResources().getInteger(R.integer.small_size);
        mediumBrush = getResources().getInteger(R.integer.medium_size);
        largeBrush = getResources().getInteger(R.integer.large_size);

        drawView.setBrushSize(smallBrush);


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
     * calls on the gesture motion detection to be used when the application is touched
     * @param event the touch event happening
     * @return if the touch was handled
     */
    @Override
    public boolean onTouchEvent(MotionEvent event){
        this.masterDetector.onTouchEvent(event);
        // Be sure to call the superclass implementation
        return super.onTouchEvent(event);
    }


    /**
     * Sets a the brush color when a paint color is selected to the input view's
     * corresponding color if it isn't the currently selected color.
     * @param view the view that is clicked to determine which color to change to.
     */
    public void paintClicked(View view) {
        drawView.setErase(false);
        drawView.setBrushSize(drawView.getLastBrushSize());
        if(view!=currPaint){
            ImageButton imgView = (ImageButton)view;
            String color = view.getTag().toString();
            drawView.setColor(color);
            imgView.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));
            currPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint));
            currPaint=(ImageButton)view;
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
                //The camera permission was denied so hide the field that holds the camera.
                cameraWindow.setVisibility(View.GONE);
            }
        }

    }

    /**
     * Sets up the camera window.
     */
    private void goGoCamera() {

        CameraWb cameraWb = new CameraWb(getApplicationContext());
//        cameraWb.setCameraOritentation(degrees);
        cameraWindow = (FrameLayout) findViewById(R.id.camera_window);
        cameraWindow.addView(cameraWb);

        //If the camera is <b>NULL</b> then hid the field that holds the camera
        if(cameraWb == null){
            cameraWindow.setVisibility(View.GONE);
        }
    }

    /**
     * Responds to clicks of the following buttons on the whiteboard:
     *  - draw_btn
     *  - erase_btn
     *  - new_btn
     *  - save_btn
     * @param view the view that the click is from
     */
    @Override
    public void onClick(View view) {
        //respond to clicks
        if (view.getId() == R.id.draw_btn) {
            //draw button clicked
            final Dialog brushDialog = new Dialog(this);
            brushDialog.setTitle("Brush size:");
            brushDialog.setContentView(R.layout.brush_chooser);

            ImageButton smallBtn = (ImageButton) brushDialog.findViewById(R.id.small_brush);
            smallBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    drawView.setBrushSize(smallBrush);
                    drawView.setLastBrushSize(smallBrush);
                    drawView.setErase(false);
                    brushDialog.dismiss();
                }
            });

            ImageButton mediumBtn = (ImageButton) brushDialog.findViewById(R.id.medium_brush);
            mediumBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    drawView.setBrushSize(mediumBrush);
                    drawView.setLastBrushSize(mediumBrush);
                    drawView.setErase(false);
                    brushDialog.dismiss();
                }
            });

            ImageButton largeBtn = (ImageButton) brushDialog.findViewById(R.id.large_brush);
            largeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    drawView.setBrushSize(largeBrush);
                    drawView.setLastBrushSize(largeBrush);
                    drawView.setErase(false);
                    brushDialog.dismiss();
                }
            });

            brushDialog.show();
        } else if (view.getId() == R.id.erase_btn) {
            //switch to erase - choose size
            final Dialog brushDialog = new Dialog(this);
            brushDialog.setTitle("Eraser size:");
            brushDialog.setContentView(R.layout.brush_chooser);

            ImageButton smallBtn = (ImageButton) brushDialog.findViewById(R.id.small_brush);
            smallBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    drawView.setErase(true);
                    drawView.setBrushSize(smallBrush);
                    brushDialog.dismiss();
                }
            });
            ImageButton mediumBtn = (ImageButton) brushDialog.findViewById(R.id.medium_brush);
            mediumBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    drawView.setErase(true);
                    drawView.setBrushSize(mediumBrush);
                    brushDialog.dismiss();
                }
            });
            ImageButton largeBtn = (ImageButton) brushDialog.findViewById(R.id.large_brush);
            largeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    drawView.setErase(true);
                    drawView.setBrushSize(largeBrush);
                    brushDialog.dismiss();
                }
            });

            brushDialog.show();
        } else if (view.getId() == R.id.new_btn) {
            //new button
            AlertDialog.Builder newDialog = new AlertDialog.Builder(this);
            newDialog.setTitle("New drawing");
            newDialog.setMessage("Start new drawing (you will lose the current drawing)?");
            newDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    drawView.startNew();
                    dialog.dismiss();
                }
            });
            newDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            newDialog.show();
        } else if (view.getId() == R.id.save_btn) {
            //save drawing
            AlertDialog.Builder saveDialog = new AlertDialog.Builder(this);
            saveDialog.setTitle("Save drawing");
            saveDialog.setMessage("Save drawing to device Gallery?");
            saveDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    //save drawing
                    drawView.setDrawingCacheEnabled(true);
                    String imgSaved = MediaStore.Images.Media.insertImage(
                            getContentResolver(), drawView.getDrawingCache(),
                            UUID.randomUUID().toString() + ".png", "drawing");
                    if (imgSaved != null) {
                        Toast savedToast = Toast.makeText(getApplicationContext(),
                                "Drawing saved to Gallery!", Toast.LENGTH_SHORT);
                        savedToast.show();
                    } else {
                        Toast unsavedToast = Toast.makeText(getApplicationContext(),
                                "Oops! Image could not be saved.", Toast.LENGTH_SHORT);
                        unsavedToast.show();
                    }
                    drawView.destroyDrawingCache();
                }
            });
            saveDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            saveDialog.show();
        }else if (view.getId() == R.id.addFile){
            fileBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(driveBtn.getVisibility() == View.GONE){
                        driveBtn.setVisibility(View.VISIBLE);
                        loadBtn.setVisibility(View.VISIBLE);
                    }else{
                        driveBtn.setVisibility(View.GONE);
                        loadBtn.setVisibility(View.GONE);
                    }
                }
        });
        }
        else if (view.getId() == R.id.drive_save){
            fileBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //TODO add drive save implementation
                }
            });
        }
        else if (view.getId() == R.id.load){
            fileBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //TODO add load implementation
                }
            });
        }

    }

    /**
     * Notified when a single-tap occurs.
     * <p/>
     * Unlike {@link OnGestureListener#onSingleTapUp(MotionEvent)}, this
     * will only be called after the detector is confident that the user's
     * first tap is not followed by a second tap leading to a double-tap
     * gesture.
     *
     * @param e The down motion event of the single-tap.
     * @return true if the event is consumed, else false
     */
    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        return false;
    }

    /**
     * Notified when a double-tap occurs.
     *
     * @param e The down motion event of the first tap of the double-tap.
     * @return true if the event is consumed, else false
     */
    @Override
    public boolean onDoubleTap(MotionEvent e) {
        return false;
    }

    /**
     * Notified when an event within a double-tap gesture occurs, including
     * the down, move, and up events.
     *
     * @param e The motion event that occurred during the double-tap gesture.
     * @return true if the event is consumed, else false
     */
    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return false;
    }

    /**
     * Notified when a tap occurs with the down {@link MotionEvent}
     * that triggered it. This will be triggered immediately for
     * every down event. All other events should be preceded by this.
     *
     * @param e The down motion event.
     */
    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    /**
     * The user has performed a down {@link MotionEvent} and not performed
     * a move or up yet. This event is commonly used to provide visual
     * feedback to the user to let them know that their action has been
     * recognized i.e. highlight an element.
     *
     * @param e The down motion event
     */
    @Override
    public void onShowPress(MotionEvent e) {

    }

    /**
     * Notified when a tap occurs with the up {@link MotionEvent}
     * that triggered it.
     *
     * @param e The up motion event that completed the first tap
     * @return true if the event is consumed, else false
     */
    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    /**
     * Notified when a scroll occurs with the initial on down {@link MotionEvent} and the
     * current move {@link MotionEvent}. The distance in x and y is also supplied for
     * convenience.
     *
     * @param e1        The first down motion event that started the scrolling.
     * @param e2        The move motion event that triggered the current onScroll.
     * @param distanceX The distance along the X axis that has been scrolled since the last
     *                  call to onScroll. This is NOT the distance between {@code e1}
     *                  and {@code e2}.
     * @param distanceY The distance along the Y axis that has been scrolled since the last
     *                  call to onScroll. This is NOT the distance between {@code e1}
     *                  and {@code e2}.
     * @return true if the event is consumed, else false
     */
    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    /**
     * Notified when a long press occurs with the initial on down {@link MotionEvent}
     * that trigged it.
     *
     * @param e The initial on down motion event that started the longpress.
     */
    @Override
    public void onLongPress(MotionEvent e) {

    }

    /**
     * Notified of a fling event when it occurs with the initial on down {@link MotionEvent}
     * and the matching up {@link MotionEvent}. The calculated velocity is supplied along
     * the x and y axis in pixels per second.
     *
     * @param e1        The first down motion event that started the fling.
     * @param e2        The move motion event that triggered the current onFling.
     * @param velocityX The velocity of this fling measured in pixels per second
     *                  along the x axis.
     * @param velocityY The velocity of this fling measured in pixels per second
     *                  along the y axis.
     * @return true if the event is consumed, else false
     */
    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if(velocityX > 0){
            //TODO add swipe from Left for contact menu

        }else if(velocityX < 0){
            if (masterOptionButtons.getVisibility() == View.GONE) {
                masterOptionButtons.setVisibility(View.VISIBLE);
                masterPaintOptions.setVisibility(View.VISIBLE);
            }else {
                masterOptionButtons.setVisibility(View.GONE);
                masterPaintOptions.setVisibility(View.GONE);
                driveBtn.setVisibility(View.GONE);
                loadBtn.setVisibility(View.GONE);
            }
        }
        return false;
    }

    /**
     * Downloads contents from provided url and displays it as the background for the current view
     *
     * Created by Tristan on 2/20/2016.
     */
    class DownloadFromURLTask  extends AsyncTask<URL, Integer, Drawable>{

        @Override
        /**
         * All asynctasks need at least one of their methods overriden. This function is where the main
         * meat of the method you want to execute will go. The result is then fed to onPostExecute
         * So you can do whatever operations you need to on the returned object.
         */
        protected Drawable doInBackground(URL... params) {
            try {
                InputStream curInputStream = (InputStream) params[0].getContent();
                //According to stack overflow, the src name portion is just a relic and really doesn't
                //do anything but don't forget to include it!
                Drawable targetDraw;
                targetDraw= Drawable.createFromStream(curInputStream, "src name");
                return targetDraw;

            } catch(Exception e) {
                Log.i("Downloadfromurl", e.getMessage());
                return null;
            }
        }
        /**
         * Executes after doInBackground. Draws image to the screen as the background of the view.
         *
         */
        @Override
        protected void onPostExecute(Drawable result) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                drawView.setBackground(result);
            }

        }
    }

}
