package com.umdcs4995.whiteboard.drawing;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.umdcs4995.whiteboard.Globals;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * Created by jnmuhich on 2/21/16.
 */
public class DrawingEvent implements Delayed {

    public static final int ACTION_DOWN = 0;
    public static final int ACTION_MOVE = 1;
    public static final int ACTION_UP = 2;

    private int action;



    private String username;

    private int actionOrder;
    private long startTime;
    private long eventTime;
    private float xValue;
    private float yValue;

    public DrawingEvent(int action, long startTime, long eventTime, float xValue, float yValue) {
        this.action = action;
        this.startTime = startTime; // time where drawing started
        this.eventTime = eventTime; // time where draw event happened
        this.xValue = xValue;
        this.yValue = yValue;

        Globals g = Globals.getInstance();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(g.getGlobalContext());
        //Extract the resource
        username = sp.getString("pref_name","NOUSERNAME");


        if (action == ACTION_UP) {
            xValue = -1;
            yValue = -1;
        }
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return eventTime - startTime;
    }

    @Override
    public int compareTo(Delayed o) {
        if (this.eventTime < ((DrawingEvent) o).eventTime) {
            return -1;
        }
        if (this.eventTime > ((DrawingEvent) o).eventTime) {
            return 1;
        }
        return 0;
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public float getxValue() {
        return xValue;
    }

    public void setxValue(float xValue) {
        this.xValue = xValue;
    }

    public float getyValue() {
        return yValue;
    }

    public void setyValue(float yValue) {
        this.yValue = yValue;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEventTime() {
        return eventTime;
    }

    public int getActionOrder() {
        return actionOrder;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
