package com.umdcs4995.whiteboard.whiteboarddata;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import com.umdcs4995.whiteboard.drawing.DrawingEvent;
import com.umdcs4995.whiteboard.drawing.DrawingView;

import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * This class represents one line segment.
 * Created by Rob on 3/21/2016.
 */
public class LineSegment {

    private String TAG = "LINESEGMENT";
    private int ordianlID;
    LinkedList<DrawingEvent> drawQueue;

    /**
     * Constructor
     * @param events The list of events that represents a line segment.
     */
    public LineSegment(int ordianlID, LinkedList<DrawingEvent> events) {
        this.ordianlID = ordianlID;
        drawQueue = events;
    }

    /**
     * Creates a timer that draws a line on the relevant view's canvas.
     * @param inRealTime If true, the line will attempt to draw at the same speed as the original
     *                   user.  If false, the line will draw instantaneously.
     * @param drawPath
     * @param drawPaint
     * @param drawCanvas
     * @param view
     * @throws InterruptedException
     */
    public void drawLine(boolean inRealTime, final Path drawPath, final Paint drawPaint, final Canvas drawCanvas,
                         final DrawingView view)
            throws InterruptedException {

        DrawingEvent de = drawQueue.get(0);
        if(de != null) {
            Log.v(TAG, "POPPING QUEUE: " + de.getStartTime());
        } else {
            Log.v(TAG, "POPPING empty QUEUE");
        }


        for(int i = 0; i < drawQueue.size(); i++) {
            final DrawingEvent e = drawQueue.get(i);
            final Handler handler = new Handler();
            final int index = i;

            Timer t = new Timer();

            //This code attempts to respect real time.
            long delay = e.getDelay(TimeUnit.MILLISECONDS);
            if(!inRealTime) {
                delay = 0;
            }

            t.schedule(new TimerTask() {
                public synchronized void run() {
                    DrawMeRunnable dmr;
                    if(index == 0) {
                        dmr = new DrawMeRunnable(e, 0.0f, 0.0f, drawPath, drawPaint, drawCanvas,
                                view);
                    } else {
                        dmr = new DrawMeRunnable(e, drawQueue.get(index-1).getxValue(),
                                drawQueue.get(index-1).getyValue(), drawPath, drawPaint, drawCanvas,
                                view);
                    }
                    handler.post(dmr);
                }
            }, delay);
        }

    }


    /**
     * This threadable private class allows the individual points to be drawn in real time,
     * matching the speed drawn by the original user.
     */
    private class DrawMeRunnable implements Runnable {

        private DrawingEvent e;
        private int index = 0;
        private float oldTouchX;
        private float oldTouchY;
        private Path drawPath;
        private Paint drawPaint;
        private Canvas drawCanvas;
        private DrawingView view;


        public DrawMeRunnable(DrawingEvent de, float oldX, float oldY, Path drawPath, Paint drawPaint,
                              Canvas drawCanvas, DrawingView view) {
            e = de;
            oldTouchX = oldX;
            oldTouchY = oldY;
            this.drawPath = drawPath;
            this.drawPaint = drawPaint;
            this.drawCanvas = drawCanvas;
            this.view = view;
        }


        public void run() {
            float touchX = e.getxValue();
            float touchY = e.getyValue();
            switch (e.getAction()) {
                case DrawingEvent.ACTION_DOWN:
                    // When user touches the View, move to that point from the old point.
                    // position to start drawing.
                    //Log.i("test", timestamp + "   ACTION_DOWN   " + touchX + "   " + touchY + "\n");
                    drawPath.moveTo(oldTouchX, oldTouchY);
                    drawPath.moveTo(touchX, touchY);
                    break;
                case DrawingEvent.ACTION_MOVE:
                    // When user moves finger, draw a path from the old point to the new point
                    // along with their touch.
                    drawPath.moveTo(oldTouchX, oldTouchY);
                    drawPath.lineTo(touchX, touchY);
                    break;
                case DrawingEvent.ACTION_UP:
                    // When user lifts finger, draw the path
                    // and reset it for the next draw.
                    drawCanvas.drawPath(drawPath, drawPaint);
                    drawPath.reset();
                    break;
                default:
                    return;
            }
            oldTouchX = touchX;
            oldTouchY = touchY;
            view.invalidate(); // this allows the onDraw method to execute
            Log.v("DRAWMERUNNABLE", "Popped out " + e.getAction() + "//" + e.getEventTime() + "//" + index);
            index++;
        }
    }

}

