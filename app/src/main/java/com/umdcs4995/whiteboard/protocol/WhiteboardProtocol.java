package com.umdcs4995.whiteboard.protocol;

import android.util.Log;

import com.umdcs4995.whiteboard.drawing.DrawingEvent;
import com.umdcs4995.whiteboard.services.SocketService;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedList;

/**
 * WhiteboardProtocol provides an interface between the socket and the client.  The SocketService
 * should call inc(String) directly and various classes should call out(...) directly.  Each
 * type of protocol to send should have a corresponding out() method.  The WhiteboardProtocol should
 * be instantiated in the singleton and only one reference should exist to it.
 * Created by rob on 2/27/16.
 */
public class WhiteboardProtocol {

    private SocketService socketService;

    /**
     * Constructor that does nothing
     */
    public WhiteboardProtocol() {

    }

    /**
     * Set the socket service to use.
     */
    public void setSocketService(SocketService ss) {
        socketService = ss;
    }

    /**
     * Takes in a JSONArray of DrawEvents encoded as JSON and sends them to the drawevent queue
     */
    public void inc(JSONArray s) {
        DrawProtocol.execute(s);
    }

    /**
     * Send out a drawing event list to the server.
     */
    public void outDrawProtocol(LinkedList<DrawingEvent> deq) {
        JSONArray output = DrawProtocol.generateJSON(deq);
        try {
            socketService.sendMessage(SocketService.Messages.DRAW_EVENT, output);
        } catch (Exception e) {
            Log.e("WhiteboardProtocol", "TODO: handle me");
        }
    }

}