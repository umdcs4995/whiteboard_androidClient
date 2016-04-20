package com.umdcs4995.whiteboard.whiteboarddata;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Handler;
import android.util.Log;

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
    private boolean LOGGING = false;
    private LinkedList<DrawingEvent> drawEvents;

    private boolean hasDrawnLive;
    private boolean boolOnscreen;
    private boolean boolHasBeenSent;

    /**
     * Constructor
     * @param events The list of events that represents a line segment.
     */
    public LineSegment(int ordianlID, LinkedList<DrawingEvent> events) {
        this.ordianlID = ordianlID;
        hasDrawnLive = false;
        drawEvents = events;
        boolHasBeenSent = false;
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
    public void drawLine(
            boolean inRealTime,
            final Path drawPath,
            final Paint drawPaint,
            final Canvas drawCanvas,
            final DrawingView view) throws InterruptedException {

        inRealTime = !hasDrawnLive;

        DrawingEvent de = drawEvents.get(0);
        if(de != null) {
            Log.v(TAG, "POPPING QUEUE: " + de.getStartTime());
        } else {
            Log.v(TAG, "POPPING empty QUEUE");
        }


        for(int i = 0; i < drawEvents.size(); i++) {
            final DrawingEvent e = drawEvents.get(i);
            final Handler handler = new Handler();
            final int index = i;

            Timer t = new Timer();

            //This code attempts to respect real time.
            long delay = e.getDelay(TimeUnit.MILLISECONDS);

            if(inRealTime) {
                t.schedule(new TimerTask() {
                    public synchronized void run() {
                        DrawMeRunnable dmr;
                        if (index == 0) {
                            dmr = new DrawMeRunnable(e, 0.0f, 0.0f, drawPath, drawPaint, drawCanvas,
                                    view);
                        } else {
                            dmr = new DrawMeRunnable(e, drawEvents.get(index - 1).getxValue(),
                                    drawEvents.get(index - 1).getyValue(), drawPath, drawPaint, drawCanvas,
                                    view);
                        }
                        handler.post(dmr);
                    }
                }, delay);
                hasDrawnLive = true;
            } else {
                DrawMeRunnable dmr;
                if (index == 0) {
                    dmr = new DrawMeRunnable(e, 0.0f, 0.0f, drawPath, drawPaint, drawCanvas,
                            view);
                    dmr.run();
                } else {
                    dmr = new DrawMeRunnable(e, drawEvents.get(index - 1).getxValue(),
                            drawEvents.get(index - 1).getyValue(), drawPath, drawPaint, drawCanvas,
                            view);
                    dmr.run();
                }
            }
        }

        boolOnscreen = true;

    }

    /**
     * Returns the event time for the LineSegment.  Note the event time is the EventTime for the
     * FIRST DrawEvent in the segment.
     */
    public long getEventTime() {
        return drawEvents.get(0).getEventTime();
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
            Paint tempPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            tempPaint.setColor(e.getColor());
            tempPaint.setAntiAlias(true);
            tempPaint.setStrokeWidth(e.getThickness());
            tempPaint.setStyle(Paint.Style.STROKE);
            tempPaint.setStrokeJoin(Paint.Join.ROUND);
            tempPaint.setStrokeCap(Paint.Cap.ROUND);
            drawPaint = tempPaint;
            switch (e.getAction()) {
                case DrawingEvent.ACTION_DOWN:
                    // When user touches the View, move to that point from the old point.
                    // position to start drawing.
                    drawPath.moveTo(oldTouchX, oldTouchY);
                    drawPath.moveTo(touchX, touchY);
                    drawCanvas.drawPath(drawPath, drawPaint);
                    break;
                case DrawingEvent.ACTION_MOVE:
                    // When user moves finger, draw a path from the old point to the new point
                    // along with their touch.
                    drawPath.moveTo(oldTouchX, oldTouchY);
                    drawPath.lineTo(touchX, touchY);
                    drawCanvas.drawPath(drawPath, drawPaint);
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
            view.invalidate();
            //if(LOGGING) Log.v("DRAWMERUNNABLE", "Popped out " + e.getAction() + "//" + e.getEventTime() + "//" + index);
            index++;
        }
    }

    /**
     * Returns true if the line is on the screen and false otherwise.  Note this variable needs to
     * be flipped off by the client if the screen is cleared.
     * @return
     */
    public boolean isOnscreen() {
        return boolOnscreen;
    }

    /**
     * Call this when the lines are cleared from the screen forever reason.  Note this needs to
     * be called from the client.
     */
    public void lineClearedFromScreen() {
        this.boolOnscreen = false;
    }

    /**
     * Sets the onscreen status to true.  Called by user generated lines so they aren't drawn a
     * second time on the next repaint request.
     */
    public void lineIsOnScreen() {
        this.boolOnscreen = true;
    }

    /**
     * Returns true if the line has been successfully sent to the server by the service.
     * Returns false if it has not.  Needed for client to server line transmission reliability.
     */
    public boolean hasBeenSent() {
        return boolHasBeenSent;
    }

    /**
     * The service change this to true confirm here if the line has been sent over the network.
     * Note that once the line has been sent there should be no reason to unsend it.  Also, if
     * the line was received from the network, this should be called so a duplicate won't be
     * sent back if the client loses connectivity.
     */
    public void lineSent() {
        boolHasBeenSent = true;
    }

    /**
     * Method returns an instance to the line segments list of DrawEvents.  This is needed
     * because the protocol currently does not respond to line segments.
     */
    public LinkedList<DrawingEvent> getDrawEvents() {
        return drawEvents;
    }
}

