package com.umdcs4995.whiteboard.whiteboarddata;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.umdcs4995.whiteboard.drawing.DrawingView;

import java.util.LinkedList;

/**
 * The Whiteboard class represents a physical whiteboard.  It contains a unique identifier along
 * with a list of associated users and lines.  A new Whiteboard should be created each time a user
 * joins a new Whiteboard.
 * Created by Rob on 3/21/2016.
 */
public class Whiteboard {

    //Private variables

    String whiteboardName;
    //List of users.
    LinkedList<LineSegment> segments;


    /**
     * Default constructor for the whiteboard.
     */
    public Whiteboard() {
        whiteboardName = null;
        segments = new LinkedList<>();
    }

    /**
     * Constructor which takes in a name.  Should be called after joining a whiteboard.
     */
    public Whiteboard(String whiteboardName) {
        this.whiteboardName = whiteboardName;
        segments = new LinkedList<>();
    }

    /**
     * Add a segment to the list.
     */
    public void addSegmentToList(LineSegment segment) {
        segments.add(segment);
    }

    /**
     * Repaint a set of line segments.  The line segments repainted are only the ones that haven't
     * been repainted yet.
     */
    public void repaintLineSegments(final Path drawPath, final Paint drawPaint, final Canvas drawCanvas,
                                    final DrawingView view) {

        for (int i = 0; i < segments.size(); i++) {
            try {
                LineSegment segment = segments.get(i);
                if(!segment.isOnscreen()) segment.drawLine(false, drawPath, drawPaint, drawCanvas, view);
            } catch (InterruptedException e) {
                Log.e("WHITEBOARD.java", "Error drawing line");
            }
        }

    }

    /**
     * Clears the view and then repaints all the line segments in the list.
     */
    public void forceRepaintSegments(final Path drawPath, final Paint drawPaint, final Canvas drawCanvas,
                                    final DrawingView view) {

        view.startNew();
        for (int i = 0; i < segments.size(); i++) {
            try {
                LineSegment segment = segments.get(i);
                if(!segment.isOnscreen()) segment.drawLine(false, drawPath, drawPaint, drawCanvas, view);
            } catch (InterruptedException e) {
                Log.e("WHITEBOARD.java", "Error drawing line");
            }
        }

    }

    /**
     * Returns a count of the number of LineSegments in the list.  Used to set the ordinal for the
     * next line segment.
     */
    public int getLineSegmentCount() {
        return segments.size();
    }

    /**
     * Broadcast to the app informing of changes to the list of line segments in the Whiteboard.
     */
    public void broadcastRepaintRequest(Context context) {
        Log.i("Whiteboard.java", "Broadcasting repaint request message.");
        Intent intent = new Intent("repaintRequest");
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

}
