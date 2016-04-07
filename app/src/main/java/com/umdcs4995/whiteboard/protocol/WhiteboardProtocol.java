package com.umdcs4995.whiteboard.protocol;

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
     * This method takes in a string.  It should take in a string, figure out the command language,
     * and pass that string to the appropriate sub-protocol execute method.
     * @param s String received by listener
     */
    @Deprecated
    public void inc(String s) throws WbProtocolException {
        String tempString;

        try {
            tempString = s.substring(0, s.indexOf('/'));
        } catch (Exception e) {
            throw new WbProtocolException("Error, no command language found on String: " + s, e);
        }

        try {

            switch (tempString) {
                case DrawProtocol.COMMANDLANGUAGE:
                    DrawProtocol.execute(s);
                    break;
                default:
                    throw new WbProtocolException("No valid command language found", null);
            }
        } catch (StringIndexOutOfBoundsException ex) {
            throw new WbProtocolException("Error: Malformed string processed: " + s, ex);
        }
    }

    /**
     * Takes in a JSONArray of DrawEvents encoded as JSON and sends them to the drawevent queue
     */
    public void inc(JSONArray s) {
        DrawProtocol.execute(s);
    }

    /**
     * Send out a drawing event to the server.  Deprecated when we decided to send whole strings
     * at a time.
     */
    @Deprecated
    public void outDrawProtocol(DrawingEvent de) {
        String output = DrawProtocol.generateOutputString(de);
        socketService.sendMessage(SocketService.Messages.DRAW_EVENT, output);
    }

    /**
     * Send out a drawing event list to the server.
     */
    public void outDrawProtocol(LinkedList<DrawingEvent> deq) {
        JSONArray output = DrawProtocol.generateJSON(deq);
        socketService.sendMessage(SocketService.Messages.DRAW_EVENT, output);
    }

}