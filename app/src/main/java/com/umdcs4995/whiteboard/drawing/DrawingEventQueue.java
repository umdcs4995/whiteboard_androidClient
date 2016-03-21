package com.umdcs4995.whiteboard.drawing;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.umdcs4995.whiteboard.Globals;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.PriorityQueue;

/**
 * This advanced queue is responsible for sorting through incoming draw events.
 * Created by rob on 2/28/16.
 */
public class DrawingEventQueue {

    private LinkedList<LinkedList<DrawingEvent>> listOfQueues;
    private LinkedList<LinkedList<DrawingEvent>> listOfFinishedQueues;
    private LinkedList<DrawingEvent> activeAddQueue;
    private String username;

    /**
     * Constructor needs to set the username and instantiate the list of queues.  It also needs
     * to start an active add queue.
     */
    public DrawingEventQueue() {
        Globals g = Globals.getInstance();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(g.getGlobalContext());
        //Extract the resource
        username = sp.getString("pref_name","NOUSERNAME");

        listOfQueues = new LinkedList<>();
        listOfFinishedQueues = new LinkedList<>();
        activeAddQueue = new LinkedList<>();
    }

    /**
     * Adds a new DrawingEvent to the respective queue.
     * @param de
     */
    public void addEvent(DrawingEvent de) {
        if(de.getUsername().equals(username)) {
            //This packet was received on an echo, just disregard.
            return;
        }

        activeAddQueue = findCorrectQueue(de);
        if(activeAddQueue == null) {
            //Then there wasn't a start time match, so make a new queue.
            activeAddQueue = new LinkedList<DrawingEvent>();
            listOfQueues.addLast(activeAddQueue);
        }

        activeAddQueue.add(de);

        if(de.getAction() == DrawingEvent.ACTION_UP) {
            //Finish off this queue.
            //listOfFinishedQueues.add(activeAddQueue);
            //listOfQueues.remove(activeAddQueue);
        }


    }

    private LinkedList<DrawingEvent> findCorrectQueue(DrawingEvent de) {
        LinkedList<DrawingEvent> checkQueue;

        for(int i = 0; i < listOfQueues.size(); i++) {
            checkQueue = listOfQueues.get(i);
            DrawingEvent compareEvent = checkQueue.peek();
            if(compareEvent.getStartTime() == de.getStartTime()) {
                return checkQueue;
            }
        }

        return null;
    }



    /**
     * Gets the next priority queue.
     */
    public LinkedList<DrawingEvent> peekPriorityQueue() {
        return listOfFinishedQueues.peek();
    }

    public LinkedList<DrawingEvent> popPriorityQueue() {
        return listOfFinishedQueues.poll();
    }

    public void addFinishedQueue(LinkedList<DrawingEvent> tempQueue) {
        DrawingEvent de = tempQueue.peek();
        if(!de.getUsername().equals(username)) {
            listOfFinishedQueues.add(tempQueue);
        }
    }
}