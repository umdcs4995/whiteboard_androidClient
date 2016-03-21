package com.umdcs4995.whiteboard.whiteboarddata;

/**
 * The Whiteboard class represents a physical whiteboard.  It contains a unique identifier along
 * with a list of associated users and lines.
 * Created by Rob on 3/21/2016.
 */
public class Whiteboard {

    //Private variables

    String whiteboardID;
    //List of users.
    //List of line segments.


    /**
     * Default constructor for the whiteboard.
     */
    public Whiteboard(String whiteboardID) {
        this.whiteboardID = whiteboardID;
    }
}
