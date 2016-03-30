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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.umdcs4995.whiteboard.AppConstants;
import com.umdcs4995.whiteboard.CameraWb;
import com.umdcs4995.whiteboard.R;
import com.umdcs4995.whiteboard.drawing.DrawingView;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

/**
 * This class contains the code for the drawing fragment.
 * Created by Rob on 3/21/2016.
 */
public class WhiteboardDrawFragment extends Fragment implements View.OnClickListener{

    private DrawingView drawView;
    private ImageButton currPaint, drawBtn, undoBtn, newBtn, saveBtn, fileBtn, loadBtn, driveBtn;


    // Test button that will load an image from a url
    private static final String TAG = "WhiteboardDrawFragment";
    //Test link to a connect the dots. good measure for it "working" would be to be
    //able to accurately draw over the dots.
    private String testURL = "http://www.connectthedots101.com/dot_to_dots_for_kids/Pachycephalosaurus/Pachycephalosaurus_with_Patches_connect_dots.png";


    //initialize brush sizes
    //TODO grab from the resource file
    private float smallBrush = 5, mediumBrush = 10, largeBrush = 15;


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

    /**
     * Executes when the activity starts. Initializes UI elements and sets up the drawing.
     * @param savedInstanceState
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        setupOnClickListeners();
        super.onActivityCreated(savedInstanceState);
        drawView = (DrawingView) getActivity().findViewById(R.id.drawing);
        currPaint = (ImageButton) getActivity().findViewById(R.id.btn_drawfrag_color1);
        //set up the Drawing view
        drawView.setupDrawing();
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
     * This method sets all the buttons onClick listeners to "this", passing the licks into
     * the onClick method below.
     */
    private void setupOnClickListeners() {
        ImageButton button;

        button = (ImageButton) getActivity().findViewById(R.id.new_btn);
        button.setOnClickListener(this);

        button = (ImageButton) getActivity().findViewById(R.id.draw_btn);
        button.setOnClickListener(this);

        button = (ImageButton) getActivity().findViewById(R.id.undo_btn);
        button.setOnClickListener(this);

        button = (ImageButton) getActivity().findViewById(R.id.save_btn);
        button.setOnClickListener(this);

        button = (ImageButton) getActivity().findViewById(R.id.erease_btn);
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
     *  - undo_btn
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
        } else if (view.getId() == R.id.undo_btn) {
            drawView.undoLastLine();
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
        else if (view.getContentDescription().equals("Paint")) {
            paintClicked(view);
        }

    }

    /**
     * Takes URL and downloads the image it refers to.
     * @param urlString The url that you want to download your image from.
     */
    public void loadBackgroundFromURL(String urlString){
        //click button code here
        //goal is to get a drawable object and then draw it to canvas put in just the right
        //layer
        Log.i(TAG, "did click the button");

        URL tempURL = null;
        //This catches if the url provided was not a valid url.
        try {
            tempURL = new URL(urlString);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        new DownloadFromURLTask().execute(tempURL);
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
                Log.i("Downloadfromurl", targetDraw.toString());
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
