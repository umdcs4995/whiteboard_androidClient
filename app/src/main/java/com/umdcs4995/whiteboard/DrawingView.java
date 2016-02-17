package com.umdcs4995.whiteboard;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

/**
 * Creates a drawing on a canvas using user input.
 */
public class DrawingView extends View{
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

    public DrawingView(Context con, AttributeSet att) {
        super(con, att);
        setupDrawing();
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
        float touchX = event.getX();
        float touchY = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // When user touches the View, move to that
                // position to start drawing.
                drawPath.moveTo(touchX, touchY);
                break;
            case MotionEvent.ACTION_MOVE:
                // When user moves finger, draw a path
                // along with their touch.
                drawPath.lineTo(touchX, touchY);
                break;
            case MotionEvent.ACTION_UP:
                // When user lifts finger, draw the path
                // and reset it for the next draw.
                drawCanvas.drawPath(drawPath, drawPaint);
                drawPath.reset();
                break;
            default:
                return false;
        }
        invalidate(); // this allows the onDraw method to execute
        return true;
    }
    /**Used to deal with a user rotating the screen. Uses a bitmap to map the screen from its old
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
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
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
}
