package com.umdcs4995.whiteboard.whiteboarddata;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.umdcs4995.whiteboard.AppConstants;
import com.umdcs4995.whiteboard.Globals;
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

    private Paint linePaint;
    private Path linePath;

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

        inRealTime = false; // !hasDrawnLive;

        DrawingEvent de = drawEvents.get(0);
        if(de != null) {
            Log.v(TAG, "POPPING QUEUE: " + de.getStartTime());
            linePath = new Path();
            linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            linePaint.setColor(de.getColor());
            linePaint.setAntiAlias(true);
            linePaint.setStrokeWidth(de.getThickness());
            linePaint.setStyle(Paint.Style.STROKE);
            linePaint.setStrokeJoin(Paint.Join.ROUND);
            linePaint.setStrokeCap(Paint.Cap.ROUND);
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
        broadcastLinePaintComplete(Globals.getInstance().getGlobalContext());

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
                    //linePath.moveTo(oldTouchX, oldTouchY);
                    linePath.moveTo(touchX, touchY);
                    drawCanvas.drawPath(linePath, linePaint);
                    break;
                case DrawingEvent.ACTION_MOVE:
                    // When user moves finger, draw a path from the old point to the new point
                    // along with their touch.
                    linePath.moveTo(oldTouchX, oldTouchY);
                    linePath.lineTo(touchX, touchY);
                    drawCanvas.drawPath(linePath, linePaint);
                    break;
                case DrawingEvent.ACTION_UP:
                    // When user lifts finger, draw the path
                    // and reset it for the next draw.
                    drawCanvas.drawPath(linePath, linePaint);
                    linePath.reset();
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

    /**
     * Broadcast to the app informing of the completion of a linepaint.
     */
    public void broadcastLinePaintComplete(Context context) {
        Log.i("LineSegment.java", "Broadcasting line painted message.");
        Intent intent = new Intent(AppConstants.BM_LINEPAINTED);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}

