package com.umdcs4995.whiteboard.protocol;

import android.renderscript.RenderScript;
import android.util.Log;

import com.umdcs4995.whiteboard.drawing.DrawingEvent;
import com.umdcs4995.whiteboard.drawing.DrawingEventQueue;
import com.umdcs4995.whiteboard.Globals;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * This class creates a string given a draw event and processes a string into a draw event.
 * Created by rob on 2/27/16.
 */
public abstract class DrawProtocol {

    /**
     * This language should be attached to the beginning of everystring of this protocol.
     */
    public final static String COMMANDLANGUAGE = "DRAWPROTOCOL";

    /**
     * Creates an output string for passing the message over the network.
     * Deprecated when we decided to send a whole stroke of drawing events at a time.
     * @param de DrawingEvent to generate a string for
     * @return A ready to send string.
     */
    @Deprecated
    public static String generateOutputString(DrawingEvent de) {
        StringBuilder builder = new StringBuilder();

        builder.append(COMMANDLANGUAGE);
        builder.append("/");

        builder.append("X:");
        builder.append(de.getxValue());
        builder.append("/");

        builder.append("Y:");
        builder.append(de.getyValue());
        builder.append("/");

        builder.append("ACTION:");
        builder.append(de.getAction());
        builder.append("/");

        builder.append("STARTIME:");
        builder.append(de.getStartTime());
        builder.append("/");

        builder.append("EVENTTIME:");
        builder.append(de.getEventTime());
        builder.append("/");

        builder.append("USER:");
        builder.append(de.getUsername());
        builder.append("/");

        return builder.toString();
    }

    /**
     * Takes an incoming string and executes it.
     */
    public static void execute(String s) {
        String TAG = "PARSER";
        final boolean logging = false;


        //Get the global instance for the drawing queue.
        Globals g = Globals.getInstance();
        DrawingEventQueue drawEventQueue = g.getDrawEventQueue();
        DrawingEvent de;

        Integer action;
        long startTime;
        long eventTime;
        float touchX;
        float touchY;
        String username;

        int parameterStart;
        int parameterEnd;

        String tempString = s;

        if(logging) Log.i(TAG, tempString);

        LinkedList<DrawingEvent> tempQueue = new LinkedList<DrawingEvent>();

        while(!tempString.isEmpty()) {
            tempString = tempString.substring(COMMANDLANGUAGE.length() + 1);
            if (logging) Log.i(TAG, tempString);

            //X Value
            parameterStart = tempString.indexOf(':');
            parameterEnd = tempString.indexOf('/');
            touchX = Float.parseFloat(tempString.substring(parameterStart + 1, parameterEnd));
            tempString = tempString.substring(parameterEnd + 1);

            if (logging) Log.i(TAG, tempString);

            //Y Value
            parameterStart = tempString.indexOf(':');
            parameterEnd = tempString.indexOf('/');
            touchY = Float.parseFloat(tempString.substring(parameterStart + 1, parameterEnd));
            tempString = tempString.substring(parameterEnd + 1);

            if (logging) Log.i(TAG, tempString);

            //Action Value
            parameterStart = tempString.indexOf(':');
            parameterEnd = tempString.indexOf('/');
            action = Integer.parseInt(tempString.substring(parameterStart + 1, parameterEnd));
            tempString = tempString.substring(parameterEnd + 1);

            if (logging) Log.i(TAG, tempString);

            //startTime Value
            parameterStart = tempString.indexOf(':');
            parameterEnd = tempString.indexOf('/');
            startTime = Long.parseLong(tempString.substring(parameterStart + 1, parameterEnd));
            tempString = tempString.substring(parameterEnd + 1);

            if (logging) Log.i(TAG, tempString);

            //eventTime Value
            parameterStart = tempString.indexOf(':');
            parameterEnd = tempString.indexOf('/');
            eventTime = Long.parseLong(tempString.substring(parameterStart + 1, parameterEnd));
            tempString = tempString.substring(parameterEnd + 1);

            if (logging) Log.i(TAG, tempString);

            //username Value
            parameterStart = tempString.indexOf(':');
            parameterEnd = tempString.indexOf('/');
            username = tempString.substring(parameterStart + 1, parameterEnd);
            tempString = tempString.substring(parameterEnd + 1);

            if (logging) Log.i(TAG, tempString);
            de = new DrawingEvent(action, startTime, eventTime, touchX, touchY);
            de.setUsername(username);

            //Add the finished drawing event to the temporary queue.
            tempQueue.add(de);
        }

        drawEventQueue.addFinishedQueue(tempQueue);
    }

    /**
     * Gets data from a DrawingEvent list and puts it in a JSONArray.
     * @param list The list of DrawingEvents being parsed.
     * @return The JSONArray representing the events being parsed.
     */
    public static JSONArray generateJSON(LinkedList<DrawingEvent> list) {
        JSONArray builder = new JSONArray();

        for(int i = 0; i < list.size(); i++) {
            JSONObject jObject = new JSONObject();
            DrawingEvent de = list.get(i);

            try {
                jObject.put("Username", de.getUsername());
                jObject.put("X", de.getxValue());
                jObject.put("Y", de.getyValue());
                jObject.put("Action", de.getAction());
                jObject.put("Start Time", de.getStartTime());
                jObject.put("Event Time", de.getEventTime());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            builder.put(jObject);
        }

        return builder;
    }

    /**
     * Creates an output string for a whole linkedlist of drawing events.
     * @param list DrawingEventList to generate a string for
     * @return A ready to send string.
     */
    public static String generateOutputString(LinkedList<DrawingEvent> list) {
        StringBuilder builder = new StringBuilder();

        for(int i = 0; i < list.size(); i++) {
            DrawingEvent de = list.get(i);

            builder.append(COMMANDLANGUAGE);
            builder.append("/");

            builder.append("X:");
            builder.append(de.getxValue());
            builder.append("/");

            builder.append("Y:");
            builder.append(de.getyValue());
            builder.append("/");

            builder.append("ACTION:");
            builder.append(de.getAction());
            builder.append("/");

            builder.append("STARTIME:");
            builder.append(de.getStartTime());
            builder.append("/");

            builder.append("EVENTTIME:");
            builder.append(de.getEventTime());
            builder.append("/");

            builder.append("USER:");
            builder.append(de.getUsername());
            builder.append("/");
        }

        return builder.toString();
    }

    /**
     * Parses a JSONArray for DrawingEvent values and adds the list
     * of DrawingEvents to a DrawingEventQueue.
     * @param ja The JSONArray being parsed.
     */
    public static void execute(JSONArray ja) {
        LinkedList<DrawingEvent> list = new LinkedList<>();
        //Get the global instance for the drawing queue.
        Globals g = Globals.getInstance();
        DrawingEventQueue drawEventQueue = g.getDrawEventQueue();


        for(int i = 0; i < ja.length(); i++) {
            Integer action;
            long startTime;
            long eventTime;
            float touchX;
            float touchY;
            String username;
            DrawingEvent de = null;

            try {
                JSONObject jo = ja.getJSONObject(i);
                touchX = (float) jo.getDouble("X");
                touchY = (float) jo.getDouble("Y");
                startTime = jo.getLong("Start Time");
                eventTime = jo.getLong("Event Time");
                action = jo.getInt("Action");
                de = new DrawingEvent(action, startTime, eventTime, touchX, touchY);
                de.setUsername("TODO CHANGE ME");

                //Add the finished drawing event to the temporary queue.
                list.add(de);
            } catch (Exception e) {
                Log.i("DrawProtocol", "JSON Error");
            }

        }
        drawEventQueue.addFinishedQueue(list);
    }
}