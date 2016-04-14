package com.umdcs4995.whiteboard.drawing;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.umdcs4995.whiteboard.Globals;
import com.umdcs4995.whiteboard.whiteboarddata.LineSegment;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.PriorityQueue;

/**
 * This advanced queue is responsible for sorting through incoming draw events.
 * Created by rob on 2/28/16.
 */
public class DrawingEventQueue {

    private LinkedList<LineSegment> listOfFinishedQueues;

    /**
     * Constructor needs to set the username and instantiate the list of queues.  It also needs
     * to start an active add queue.
     */
    public DrawingEventQueue() {
        //Extract the resource
        listOfFinishedQueues = new LinkedList<>();
    }


    /**
     * Gets the next priority queue.
     */
    public LineSegment peekPriorityQueue() {
        return listOfFinishedQueues.peek();
    }

    public LineSegment popPriorityQueue() {
        return listOfFinishedQueues.poll();
    }

    public void addFinishedQueue(LineSegment tempQueue) {
        listOfFinishedQueues.add(tempQueue);
    }
}