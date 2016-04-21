package com.umdcs4995.whiteboard.uiElements;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Bitmap;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.umdcs4995.whiteboard.AppConstants;
import com.umdcs4995.whiteboard.CameraWb;
import com.umdcs4995.whiteboard.Globals;
import com.umdcs4995.whiteboard.MainActivity;
import com.umdcs4995.whiteboard.R;
import com.umdcs4995.whiteboard.drawing.DrawingView;
import com.umdcs4995.whiteboard.services.SocketService;
import com.umdcs4995.whiteboard.whiteboarddata.Whiteboard;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

/**
 * This class contains the code for the drawing fragment.  This fragment should be enclosed in the
 * main activity.
 * Created by Rob on 3/21/2016.
 */
public class WhiteboardDrawFragment extends Fragment implements View.OnClickListener{

    private static boolean drawMode = false;
    private static DrawingView drawView;
    private static ImageButton currPaint, drawBtn, undoBtn, newBtn, saveBtn, eraseBtn;
    private boolean broadcastReceiverSetup = false;

    //Color Options
    private static ImageButton c1, c2, c3, c4, c5, c6, c7, c8, c9, c10, c11, c12;

    // Test button that will load an image from a url
    private static final String TAG = "WhiteboardDrawFragment";

    //Test link to a connect the dots. good measure for it "working" would be to be
    //able to accurately draw over the dots.
    private String testURL = "http://www.connectthedots101.com/dot_to_dots_for_kids/Pachycephalosaurus/Pachycephalosaurus_with_Patches_connect_dots.png";

    //Flag so that the DrawingView.setup() method is only called once.
    private boolean drawingViewSetup = false;

    //initialize brush sizes
    //TODO grab from the resource file
    private float smallBrush = 5, mediumBrush = 10, largeBrush = 15;


    //Camera Window
    private FrameLayout cameraWindow;


    /**
     * Receiver for repaint requests.
     */
    private BroadcastReceiver bRepaintRequestReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("DRAWINGVIEW", "Received repaint request broadcast");
            Whiteboard wb = Globals.getInstance().getWhiteboard();
            try {
                wb.repaintLineSegments(drawView.getDrawPath(),
                        drawView.getDrawPaint(), drawView.getDrawCanvas(), drawView);
            } catch(NullPointerException e) {
                Log.e("DRAWINGVIEW", "Catch Nullpointer processing Whiteboard.repaintLineSegments()");
                e.printStackTrace();
            } catch(Exception e) {
                Log.e("WHITEBOARDDRAWFRAGMENT", "Exception: " + e.toString());
                e.printStackTrace();
            }
        }
    };

    /**
     * Receiver for reconnection notifications.
     */
    private BroadcastReceiver brReconnectionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("MAINACTIVITY", "Received reconnection broadcast.");
            Whiteboard wb = Globals.getInstance().getWhiteboard();
            if(wb != null) {
                //Attempt to rejoin whiteboard.
                JSONObject joinWbRequest = new JSONObject();
                try {
                    joinWbRequest.put("name", wb.getWhiteboardName());
                } catch (JSONException e) {
                    Log.i("brReconnectionReceiver", "failed to join whiteboard");
                }

                final SocketService socket = Globals.getInstance().getSocketService();

                try {
                    socket.sendMessage(SocketService.Messages.JOIN_WHITEBOARD, joinWbRequest);
                } catch (Exception e) {
                    Log.e("CreateBoard", "Not connected to server");
                }

                wb.resendUnsentFragments();
            }

        }
    };

    /**
     * Receiver for line painting completion notifications.  Note one of these should be received
     * for each line.
     */
    private BroadcastReceiver bLinePaintCompleted = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("DRAWINGVIEW", "Received line paint complete broadcast");
            Globals.getInstance().decrementPaintCount();
        }
    };





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
     * This method is the last method called prior to the fragment being displayed.  It is called
     * each time the app is restored and each time the fragment is set to appear.
     */
    @Override
    public void onResume() {
        super.onResume();

        //Check to see if a Whiteboard is loaded and toggle UI elements accordingly.
        MainActivity activity = (MainActivity)getActivity();
        TextView tv = (TextView) getActivity().findViewById(R.id.textView_join_whiteboard_warning);
        Whiteboard wb = Globals.getInstance().getWhiteboard();

        if(wb != null) {
            activity.toggleFABVisibility(true);
            tv.setVisibility(View.GONE);
            setDrawMode(true);
        } else {
            activity.toggleFABVisibility(false);
            tv.setVisibility(View.VISIBLE);
            setDrawMode(false);
        }


        drawView.setDrawingCacheEnabled(true);

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            byte[] bytes = bundle.getByteArray("image");
            Log.d(TAG, "got the byte array exra");
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            Log.d(TAG, "decoded the byte array");
            drawView.setCanvasBitmap(bitmap);
            if (bitmap != null) {
                Drawable drawBitMap = new BitmapDrawable(getResources(), bitmap);
                drawView.setBackground(drawBitMap);
            }

        }
    }

    /**
     * Sets up the activity with its necessary components
     * including setting up the brush color and size
     * also sets up the drawView, which retrieves the current drawing
     * @param savedInstanceState
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        setupOnClickListeners();
        super.onActivityCreated(savedInstanceState);

        drawView = (DrawingView) getActivity().findViewById(R.id.drawing);

        if(!broadcastReceiverSetup) {
            registerBroadcastReceiver();
            broadcastReceiverSetup = true;
        }
        drawView.setupDrawing();

        currPaint = (ImageButton) getActivity().findViewById(R.id.btn_drawfrag_color1);
        drawView.setBrushSize(smallBrush);//sets initial brush size

    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Sets a the brush color when a paint color is selected to the input view's
     * corresponding color if it isn't the currently selected color.
     * @param view the view that is clicked to determine which color to change to.
     */
    public void paintClicked(View view) {
        drawView.setErase(false);
        //drawView.setBrushSize(drawView.getLastBrushSize());
        if(view!=currPaint){
            ImageButton imgView = (ImageButton)view;
            int color = Color.parseColor(view.getTag().toString());
            drawView.setColor(color);
            imgView.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));
            currPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint));
            currPaint=(ImageButton)view;
        }

    }

    /**
     * Registers the broadcast receiver.
     */
    private void registerBroadcastReceiver() {
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(Globals.getInstance().getGlobalContext());

        //Register the intent receiver so that the view updates upon receiving.
        lbm.registerReceiver(bRepaintRequestReceiver,
                new IntentFilter("repaintRequest"));

        //Register the intent receiver so that the view updates upon receiving.
        lbm.registerReceiver(brReconnectionReceiver,
                new IntentFilter(AppConstants.BM_RECONNECTED));

        lbm.registerReceiver(bLinePaintCompleted,
                new IntentFilter(AppConstants.BM_LINEPAINTED));

    }

    /**
     * This method sets all the buttons onClick listeners to "this", passing the clicks into
     * the onClick method below.
     */
    private void setupOnClickListeners() {

        //Drawing View and Buttons
        drawBtn = (ImageButton) getActivity().findViewById(R.id.draw_btn);
        drawBtn.setOnClickListener(this);

        eraseBtn = (ImageButton) getActivity().findViewById(R.id.erease_btn);
        eraseBtn.setOnClickListener(this);

        newBtn = (ImageButton) getActivity().findViewById(R.id.new_btn);
        newBtn.setOnClickListener(this);

        saveBtn = (ImageButton) getActivity().findViewById(R.id.save_btn);
        saveBtn.setOnClickListener(this);

        undoBtn = (ImageButton) getActivity().findViewById(R.id.undo_btn);
        undoBtn.setOnClickListener(this);
        //Done Editing Horizontal Buttons

        //Vertical Colors Menu
        c1 = (ImageButton) getActivity().findViewById(R.id.btn_drawfrag_color1);
        c1.setOnClickListener(this);
        c2 = (ImageButton) getActivity().findViewById(R.id.btn_drawfrag_color2);
        c2.setOnClickListener(this);
        c3 = (ImageButton) getActivity().findViewById(R.id.btn_drawfrag_color3);
        c3.setOnClickListener(this);
        c4 = (ImageButton) getActivity().findViewById(R.id.btn_drawfrag_color4);
        c4.setOnClickListener(this);
        c5 = (ImageButton) getActivity().findViewById(R.id.btn_drawfrag_color5);
        c5.setOnClickListener(this);
        c6 = (ImageButton) getActivity().findViewById(R.id.btn_drawfrag_color6);
        c6.setOnClickListener(this);
        c7 = (ImageButton) getActivity().findViewById(R.id.btn_drawfrag_color7);
        c7.setOnClickListener(this);
        c8 = (ImageButton) getActivity().findViewById(R.id.btn_drawfrag_color8);
        c8.setOnClickListener(this);
        c9 = (ImageButton) getActivity().findViewById(R.id.btn_drawfrag_color9);
        c9.setOnClickListener(this);
        c10 = (ImageButton) getActivity().findViewById(R.id.btn_drawfrag_color10);
        c10.setOnClickListener(this);
        c11 = (ImageButton) getActivity().findViewById(R.id.btn_drawfrag_color11);
        c11.setOnClickListener(this);
        c12 = (ImageButton) getActivity().findViewById(R.id.btn_drawfrag_color12);
        c12.setOnClickListener(this);
        //End Colors
    }




    /**
     * This method is called when the user clicks "Grant" or "Deny" for any permission.
     * For this activity, that should only be a camera request.  None-the-less, to extend this
     * code, set a constant in the AppConstants class and then add another if statement for
     * the new request code.  This is only necessary in Android 6.0+
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
//        cameraWb.setCameraOrientation(degrees);
        cameraWindow = (FrameLayout) getActivity().findViewById(R.id.camera_window);
        cameraWindow.addView(cameraWb);

        //If the camera is <b>NULL</b> then hid the field that holds the camera
        if(cameraWb == null){
            cameraWindow.setVisibility(View.GONE);
        }
    }

    /**
     * Responds to clicks of the following buttons on the whiteboard:
     *  - draw_btn handles the size of the brush
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

            //small brush option
            ImageButton smallBtn = (ImageButton) brushDialog.findViewById(R.id.small_brush);
            smallBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    drawView.setBrushSize(smallBrush);
                    drawView.setErase(false);
                    brushDialog.dismiss();
                }
            });

            //medium brush option
            ImageButton mediumBtn = (ImageButton) brushDialog.findViewById(R.id.medium_brush);
            mediumBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    drawView.setBrushSize(mediumBrush);
                    drawView.setErase(false);
                    brushDialog.dismiss();
                }
            });

            //large brush option
            ImageButton largeBtn = (ImageButton) brushDialog.findViewById(R.id.large_brush);
            largeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    drawView.setBrushSize(largeBrush);
                    drawView.setErase(false);
                    brushDialog.dismiss();
                }
            });

            brushDialog.show();
        }
        //undo button, undoes last brush stroke
        else if (view.getId() == R.id.undo_btn) {
            drawView.undoLastLine();
        }
        //new drawing button, prompts user to make sure they want to proceed
        else if (view.getId() == R.id.new_btn) {
            AlertDialog.Builder newDialog = new AlertDialog.Builder(getContext());
            newDialog.setTitle("New drawing");
            newDialog.setMessage("Start new drawing (you will lose the current drawing)?");
            newDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    drawView.startNew();
                    drawView.clearQueue();
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
        }
        else if (view.getContentDescription().equals("Paint")) {
            paintClicked(view);
        }

    }

    /**
     * Currently tied to the fab button this function hides or un-hides the toolbar
     * Both the top / side drawing menus are shown or hidden when this function is called
     */
    public void fabHideMenu(){
        //set all the components to Visible or Gone
        if (newBtn.getVisibility() == View.GONE) {
                setDrawMode(true);
                undoBtn.setVisibility(View.VISIBLE);
                newBtn.setVisibility(View.VISIBLE);
                eraseBtn.setVisibility(View.VISIBLE);
                drawBtn.setVisibility(View.VISIBLE);
                saveBtn.setVisibility(View.VISIBLE);
                c1.setVisibility(View.VISIBLE);
                c2.setVisibility(View.VISIBLE);
                c3.setVisibility(View.VISIBLE);
                c4.setVisibility(View.VISIBLE);
                c5.setVisibility(View.VISIBLE);
                c6.setVisibility(View.VISIBLE);
                c7.setVisibility(View.VISIBLE);
                c8.setVisibility(View.VISIBLE);
                c9.setVisibility(View.VISIBLE);
                c10.setVisibility(View.VISIBLE);
                c11.setVisibility(View.VISIBLE);
                c12.setVisibility(View.VISIBLE);
        } else {
            setDrawMode(false);
            undoBtn.setVisibility(View.GONE);
            newBtn.setVisibility(View.GONE);
            eraseBtn.setVisibility(View.GONE);
            drawBtn.setVisibility(View.GONE);
            saveBtn.setVisibility(View.GONE);
            c1.setVisibility(View.GONE);
            c2.setVisibility(View.GONE);
            c3.setVisibility(View.GONE);
            c4.setVisibility(View.GONE);
            c5.setVisibility(View.GONE);
            c6.setVisibility(View.GONE);
            c7.setVisibility(View.GONE);
            c8.setVisibility(View.GONE);
            c9.setVisibility(View.GONE);
            c10.setVisibility(View.GONE);
            c11.setVisibility(View.GONE);
            c12.setVisibility(View.GONE);
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

    public boolean getDrawMode(){
        return drawMode;
    }

    public void setDrawMode(boolean mode){
        drawMode = mode;
    }



    public Bitmap getMyBitmap() {
        return drawView.getCanvasBitmap();
    }

}