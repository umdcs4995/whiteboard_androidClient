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
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
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
public class MasterWhiteboardAddActivity extends AppCompatActivity implements View.OnClickListener {

    private DrawingView drawView;
    private ImageButton currPaint, drawBtn, eraseBtn, newBtn, saveBtn;

    //TM:
    // Test button that will load an image from a url
    private Button loadImageBtn;
    private static final String TAG = MasterWhiteboardAddActivity.class.getSimpleName();
    //Test link to a connect the dots. good measure for it "working" would be to be
    //able to accurately draw over the dots.
    private String testURL = "http://www.connectthedots101.com/dot_to_dots_for_kids/Pachycephalosaurus/Pachycephalosaurus_with_Patches_connect_dots.png";
    //end TM

    private float smallBrush, mediumBrush, largeBrush;

    /**
     * Creates the floating action button, the drawing board, and initializes
     * the draw, erase, save, and new buttons. Also chooses the sizes for the paint brushes.
     * @param savedInstanceState used for the super onCreate()
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_master_whiteboard_add);

        drawView = (DrawingView)findViewById(R.id.drawing);
        LinearLayout paintLayout = (LinearLayout)findViewById(R.id.paint_colors);

        currPaint = (ImageButton)paintLayout.getChildAt(0);
        currPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));

        drawBtn = (ImageButton)findViewById(R.id.draw_btn);
        drawBtn.setOnClickListener(this);

        eraseBtn = (ImageButton)findViewById(R.id.erase_btn);
        eraseBtn.setOnClickListener(this);

        newBtn = (ImageButton)findViewById(R.id.new_btn);
        newBtn.setOnClickListener(this);

        saveBtn = (ImageButton)findViewById(R.id.save_btn);
        saveBtn.setOnClickListener(this);

        smallBrush = getResources().getInteger(R.integer.small_size);
        mediumBrush = getResources().getInteger(R.integer.medium_size);
        largeBrush = getResources().getInteger(R.integer.large_size);

        drawView.setBrushSize(mediumBrush);

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
        //TM
        //This part (so far) is just Android 101 setting up a button
        loadImageBtn = (Button) findViewById(R.id.loadImageBtn);
        loadImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //click button code here
                //goal is to get a drawable object and then draw it to canvas put in just the right
                //layer
                Log.i(TAG, "did click the button");

                URL tempURL = null;
                try {
                    tempURL = new URL(testURL);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                new DownloadFromURLTask().execute(tempURL);

            }
        });

        //endTM

    }


    /**
     * Created by Tristan on 2/20/2016.
     */
    class DownloadFromURLTask  extends AsyncTask<URL, Integer, Drawable>{

        @Override
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
        @Override
        protected void onPostExecute(Drawable result) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                drawView.setBackground(result);
            }

        }
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
                //Lets do nothing, the camera permission was denied.
            }
        }

    }

    /**
     * Sets up the camera window.
     */
    private void goGoCamera() {

        CameraWb cameraWb = new CameraWb(getApplicationContext());
//        cameraWb.setCameraOritentation(degrees);
        FrameLayout cameraWindow = (FrameLayout) findViewById(R.id.camera_window);
        cameraWindow.addView(cameraWb);
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
        Log.i(MasterWhiteboardAddActivity.class.getSimpleName(), "OMG just let me log something");
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
        }

    }
}
