package com.umdcs4995.whiteboard.drawing;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.gesture.GestureOverlayView;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.support.v4.content.LocalBroadcastManager;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.umdcs4995.whiteboard.Globals;
import com.umdcs4995.whiteboard.MainActivity;
import com.umdcs4995.whiteboard.protocol.WhiteboardProtocol;
import com.umdcs4995.whiteboard.uiElements.WhiteboardDrawFragment;
import com.umdcs4995.whiteboard.whiteboarddata.LineSegment;
import com.umdcs4995.whiteboard.whiteboarddata.Whiteboard;

import java.util.LinkedList;

/**
 * Creates a drawing on a canvas using user input.
 */
public class DrawingView extends View implements GestureOverlayView.OnGestureListener{
    //drawing path
    private Path drawPath;
    //drawing and canvas paint
    private Paint drawPaint, canvasPaint;
    //initial color
    private int paintColor = 0xFF660000;


    //canvas
    private Canvas drawCanvas;
    //canvas bitmap
    private Bitmap canvasBitmap;
    //brush size and previous size
    private float brushSize, lastBrushSize;
    //A placeholder representing the currently drawn line.
    private LinkedList<DrawingEvent> currentLineList = new LinkedList<>();
    private Boolean firstDrawEvent = true;
    private long startTime = -1;

//TODO delete
    //Width and the height of the canvas
    int canvasW = -1;
    int canvasH = -1;



    //Network interaction member items.
    private DrawingEventQueue drawingEventQueue;
    private WhiteboardProtocol protocol;


    public Paint getDrawPaint() {
        return drawPaint;
    }

    public Path getDrawPath() {
        return drawPath;
    }


    private Thread pollingThread = new Thread(new Runnable() {
        boolean newLineDetected = false;

        @Override
        public void run() {
            while (true) {
                final LineSegment drawQueue = drawingEventQueue.peekPriorityQueue();
                if (drawQueue == null) {
                    //Sleep for half second if queue is empty.
                    try {
                        if(newLineDetected) {
                            Activity activity = (MainActivity) getContext();
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        //Clear the screen.
                                        startNew();
                                        //Redraw all the old ones.
                                        Globals.getInstance().getWhiteboard().repaintLineSegments(drawPath, drawPaint, drawCanvas, getThis());
                                    } catch (NullPointerException ex) {
                                        //Most likely the DrawingView.getThis() method hasn't been established.  Just handle it and wait.
                                        Log.e("DRAWINGVIEW", "Nullpointer in repaintLineSegments()");
                                    }
                                }
                            });
                            newLineDetected = false;
                        }
                        Thread.sleep(500);
                    } catch (InterruptedException ex) {
                        //Do nothing
                    }
                } else {
                    Log.v("NewLineDetected", "Detected Line" + drawQueue.getEventTime());
                    newLineDetected = true;
                    Activity activity = (MainActivity) getContext();
                    activity.runOnUiThread(new PollingRunnable(drawQueue, getThis()));
                    drawingEventQueue.popPriorityQueue();
                }
            }
        }
    });

    public DrawingView(Context con, AttributeSet att) {
        super(con, att);
        Globals g = Globals.getInstance();
        protocol = g.getWhiteboardProtocol();
        drawingEventQueue = g.getDrawEventQueue();

        if(!pollingThread.isAlive()) {
            //pollingThread.start();
        }


    }

    /**
     * Initializes the drawing canvas.
     */
    public void setupDrawing(){
        brushSize = 5;
        lastBrushSize = brushSize;

        drawPath = new Path();
        drawPaint = new Paint();
        drawPaint.setColor(paintColor);
        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(brushSize);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);
        canvasPaint = new Paint(Paint.DITHER_FLAG);
    }

    /**
     * Executes when the user touches to draw on the screen. Draws path to the screen using current
     * brush.
     * @param canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
        //draw view
        canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
        canvas.drawPath(drawPath, drawPaint);
    }

    /**
     * Detects touch and initiates the onDraw() method
     * @param event the motion
     * Responds to touch events given by the user in order to draw lines (paths)
     * onto the canvas. A path is created by touching a point on the canvas (ACTION_DOWN),
     * moving around (ACTION_MOVE), then lifting the finger (ACTION_UP).
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //detect user touch

        if (firstDrawEvent) {
            startTime = System.currentTimeMillis();
            firstDrawEvent = false;
        }
        float touchX = event.getX();
        float touchY = event.getY();
        Long eventTime = System.currentTimeMillis();

        DrawingEvent de;

        MainActivity ma = (MainActivity) getContext();

        if(ma.isDrawModeEnabled()) {
            //DrawMode handling
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // When user touches the View, move to that
                    // position to start drawing.
                    de = new DrawingEvent(DrawingEvent.ACTION_DOWN, startTime,
                            eventTime, touchX, touchY);
                    currentLineList = new LinkedList<>();
                    currentLineList.add(de);
                    drawPath.moveTo(touchX, touchY);
                    break;
                case MotionEvent.ACTION_MOVE:
                    // When user moves finger, draw a path
                    // along with their touch.
                    de = new DrawingEvent(DrawingEvent.ACTION_MOVE, startTime,
                            eventTime, touchX, touchY);
                    currentLineList.add(de);
                    drawPath.lineTo(touchX, touchY);
                    break;
                case MotionEvent.ACTION_UP:
                    // When user lifts finger, draw the path
                    // and reset it for the next draw.
                    de = new DrawingEvent(DrawingEvent.ACTION_UP, startTime,
                            eventTime, touchX, touchY);
                    currentLineList.add(de);
                    protocol.outDrawProtocol(currentLineList);
                    Whiteboard wb = Globals.getInstance().getWhiteboard();
                    LineSegment ls = new LineSegment(wb.getLineSegmentCount(), currentLineList);
                    wb.addSegmentToList(ls);
                    ls.lineIsOnScreen();
                    drawCanvas.drawPath(drawPath, drawPaint);
                    drawPath.reset();
                    firstDrawEvent = true; //<- Added to reset the start time on the next stroke.
                    break;
                default:
                    return false;
            }
            invalidate(); // this allows the onDraw method to execute
            return true;
        } else {
            // View Mode Handling
            // TODO: Pinch to zoom / scroll & anything else in the view mode
        }
        return true;
    }


    /** Used to deal with a user rotating the screen. Uses a bitmap to map the screen from its old
     * orientation to the new one.
     * @param w
     * @param h
     * @param oldw
     * @param oldh
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        //view given size
        super.onSizeChanged(w, h, oldw, oldh);
        if(canvasBitmap != null) {
            canvasBitmap = canvasBitmap.copy(Bitmap.Config.ARGB_8888, true);
        }
        else {
            Bitmap immutableBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            canvasBitmap = immutableBitmap.copy(Bitmap.Config.ARGB_8888, true);
        }
        canvasW = w;
        canvasH = h;

        drawCanvas = new Canvas(canvasBitmap);
    }

    /**
     * Changes the color of the "pen" being used.
     * @param newColor
     */
    public void setColor(String newColor){
        //set color
        invalidate();
        paintColor = Color.parseColor(newColor);
        drawPaint.setColor(paintColor);
    }

    /**
     * Changes the size of the "brush" being used.
     * @param newSize
     */
    public void setBrushSize(float newSize){
        //update size
        float pixelAmount = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                newSize, getResources().getDisplayMetrics());
        brushSize=pixelAmount;
        drawPaint.setStrokeWidth(brushSize);
    }

    /**
     * Sets the "brush" size to the last known brush size (when changing colors).
     * @param lastSize
     */
    public void setLastBrushSize(float lastSize){
        lastBrushSize=lastSize;
    }

    /**
     * Gets the last known "brush" size (for changing colors).
     * @return
     */
    public float getLastBrushSize(){
        return lastBrushSize;
    }

    /**
     * Checks if the user is using the eraser.
     * If so it "paints" in white, otherwise it used the previous color.
     * @param isErase
     */
    public void setErase(boolean isErase){
        //set erase true or false
        if(isErase) {
            drawPaint.setColor(Color.WHITE); // Set color to white
        } else {
            drawPaint.setColor(paintColor); // if erase is set to false, it will
                                            // use the previous color
        }
    }

    /**
     * Creates a new draw canvas.
     * Resets the canvas to be blank.
     */
    public void startNew(){
        drawCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
        invalidate();
    }
    /**
     * Returns the canvas being used by this view
     */
    public Canvas getDrawCanvas() {
        return drawCanvas;
    }

    /**
     * Return a reference to the instance of this view.
     */
    public DrawingView getThis() {
        return DrawingView.this;
    }

    /**
     * Undo the last line that has been drawn.
     * The line must not be empty
     * If the line is interrupted finishes where it was interrupted and does not finish the drawing
     */
    public void undoLastLine() {
        //TODO: Fix this.
//        if(lineHistory.size() > 0) {
//            lineHistory.removeLast();
//            startNew();
//            for (LineSegment ls : lineHistory) {
//                try {
//                    ls.drawLine(false, drawPath, drawPaint, drawCanvas, getThis());
//                } catch (InterruptedException e) {
//
//                }
//            }
//        }
    }

    /**
     * Clears the queue from drawing that are stored
     */
    public void clearQueue(){
        //TODO: Fix this.
//        lineHistory.clear();
    }


    /**
     * This class creates a runnable to parse through an incoming network line event.
     */
    private class PollingRunnable implements Runnable{
        private LineSegment ls;
        private DrawingView view;

        public PollingRunnable(LineSegment ls, DrawingView view) {
            this.ls = ls;
            this.view = view;
        }

        @Override
        public void run() {
            try {
                ls.drawLine(false, drawPath, drawPaint, drawCanvas, view);
            } catch(Exception e) {
                Log.e("POLLINGRUNNABLE", "Error drawing string");
            }
        }
    }

    public Bitmap getCanvasBitmap() {
        return canvasBitmap;
    }

    public void setCanvasBitmap(Bitmap bitmap) {
//        Log.d(TAG, "in setCanvasBitmap");
        Bitmap bm = Bitmap.createBitmap(100, 100, Config.ARGB_8888);
        canvasBitmap = bitmap.copy(Config.ARGB_8888, true);
        drawCanvas = new Canvas(canvasBitmap);

        drawCanvas.drawBitmap(canvasBitmap, 0, 0, new Paint(Color.RED));

        super.draw(drawCanvas);
        this.postInvalidate();
        //drawCanvas.setBitmap(Bitmap.createBitmap(bitmap, 0, 0, drawCanvas.getWidth(), drawCanvas.getHeight()));
    }

    @Override
    public void onGestureStarted(GestureOverlayView overlay, MotionEvent event) {

    }

    @Override
    public void onGesture(GestureOverlayView overlay, MotionEvent event) {

    }

    @Override
    public void onGestureEnded(GestureOverlayView overlay, MotionEvent event) {

    }

    @Override
    public void onGestureCancelled(GestureOverlayView overlay, MotionEvent event) {

    }


}
