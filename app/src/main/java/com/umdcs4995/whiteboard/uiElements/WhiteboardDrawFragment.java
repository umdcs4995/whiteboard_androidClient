package com.umdcs4995.whiteboard.uiElements;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.umdcs4995.whiteboard.AppConstants;
import com.umdcs4995.whiteboard.CameraWb;
import com.umdcs4995.whiteboard.MainActivity;
import com.umdcs4995.whiteboard.R;
import com.umdcs4995.whiteboard.drawing.DrawingView;

import java.io.InputStream;
import java.net.URL;
import java.util.UUID;

/**
 * This class contains the code for the drawing fragment.
 * Created by Rob on 3/21/2016.
 */
public class WhiteboardDrawFragment extends Fragment implements View.OnClickListener,
        GestureDetector.OnGestureListener,
        GestureDetector.OnDoubleTapListener {

    private DrawingView drawView;
    private ImageButton currPaint, drawBtn, eraseBtn, newBtn, saveBtn, fileBtn, loadBtn, driveBtn;


    // Test button that will load an image from a url
    private static final String TAG = "WhiteboardDrawFragment";
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

    @Nullable
    @Override
    /**
     * Required to use fragment.  This is called each time the fragment is made "active" by the
     * activity.  Inflate the XML layout and return that view.
     */
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_whiteboard_draw, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setupOnClickListeners();
        drawView = (DrawingView) getActivity().findViewById(R.id.drawing);
        currPaint = (ImageButton) getActivity().findViewById(R.id.btn_drawfrag_color1);
    }


    /**
     * calls on the gesture motion detection to be used when the application is touched
     * @param event the touch event happening
     * @return if the touch was handled
     */
//    @Override
//    public boolean onTouchEvent(MotionEvent event){
//        this.masterDetector.onTouchEvent(event);
//        // Be sure to call the superclass implementation
//        return getActivity().onTouchEvent(event);
//    }


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
     * This method sets all the buttons onClick listeners to "this", passing the licks into
     * the onClick method below.
     */
    private void setupOnClickListeners() {
        ImageButton button;

        button = (ImageButton) getActivity().findViewById(R.id.new_btn);
        button.setOnClickListener(this);

        button = (ImageButton) getActivity().findViewById(R.id.draw_btn);
        button.setOnClickListener(this);

        button = (ImageButton) getActivity().findViewById(R.id.erase_btn);
        button.setOnClickListener(this);

        button = (ImageButton) getActivity().findViewById(R.id.save_btn);
        button.setOnClickListener(this);

        button = (ImageButton) getActivity().findViewById(R.id.addFile);
        button.setOnClickListener(this);

        button = (ImageButton) getActivity().findViewById(R.id.drive_save);
        button.setOnClickListener(this);

        button = (ImageButton) getActivity().findViewById(R.id.btn_drawfrag_color1);
        button.setOnClickListener(this);
        button = (ImageButton) getActivity().findViewById(R.id.btn_drawfrag_color2);
        button.setOnClickListener(this);
        button = (ImageButton) getActivity().findViewById(R.id.btn_drawfrag_color3);
        button.setOnClickListener(this);
        button = (ImageButton) getActivity().findViewById(R.id.btn_drawfrag_color4);
        button.setOnClickListener(this);
        button = (ImageButton) getActivity().findViewById(R.id.btn_drawfrag_color5);
        button.setOnClickListener(this);
        button = (ImageButton) getActivity().findViewById(R.id.btn_drawfrag_color6);
        button.setOnClickListener(this);
        button = (ImageButton) getActivity().findViewById(R.id.btn_drawfrag_color7);
        button.setOnClickListener(this);
        button = (ImageButton) getActivity().findViewById(R.id.btn_drawfrag_color8);
        button.setOnClickListener(this);
        button = (ImageButton) getActivity().findViewById(R.id.btn_drawfrag_color9);
        button.setOnClickListener(this);
        button = (ImageButton) getActivity().findViewById(R.id.btn_drawfrag_color10);
        button.setOnClickListener(this);
        button = (ImageButton) getActivity().findViewById(R.id.btn_drawfrag_color11);
        button.setOnClickListener(this);
        button = (ImageButton) getActivity().findViewById(R.id.btn_drawfrag_color12);
        button.setOnClickListener(this);


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

        CameraWb cameraWb = new CameraWb(getActivity().getApplicationContext());
//        cameraWb.setCameraOritentation(degrees);
        cameraWindow = (FrameLayout) getActivity().findViewById(R.id.camera_window);
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
            final Dialog brushDialog = new Dialog(getContext());
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
            final Dialog brushDialog = new Dialog(getContext());
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
            AlertDialog.Builder newDialog = new AlertDialog.Builder(getContext());
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
            AlertDialog.Builder saveDialog = new AlertDialog.Builder(getContext());
            saveDialog.setTitle("Save drawing");
            saveDialog.setMessage("Save drawing to device Gallery?");
            saveDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    //save drawing
                    drawView.setDrawingCacheEnabled(true);
                    String imgSaved = MediaStore.Images.Media.insertImage(
                            getActivity().getContentResolver(), drawView.getDrawingCache(),
                            UUID.randomUUID().toString() + ".png", "drawing");
                    if (imgSaved != null) {
                        Toast savedToast = Toast.makeText(getActivity().getApplicationContext(),
                                "Drawing saved to Gallery!", Toast.LENGTH_SHORT);
                        savedToast.show();
                    } else {
                        Toast unsavedToast = Toast.makeText(getActivity().getApplicationContext(),
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
        else if (view.getContentDescription().equals("Paint")) {
            paintClicked(view);
        }

    }

    /**
     * Notified when a single-tap occurs.
     * <p/>
     * Unlike { OnGestureListener#onSingleTapUp(MotionEvent)}, this
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
    class DownloadFromURLTask  extends AsyncTask<URL, Integer, Drawable> {

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
