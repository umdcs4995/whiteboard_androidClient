package com.umdcs4995.whiteboard.protocol;

import android.util.Log;

import com.umdcs4995.whiteboard.Globals;
import com.umdcs4995.whiteboard.whiteboarddata.Buddy;
import com.umdcs4995.whiteboard.whiteboarddata.Whiteboard;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;

/**
 * This class takes in a JSONArray object and parses it into a list of buddies to attach to
 * a Whiteboard.
 * Created by Rob on 4/25/2016.
 */
public class BuddyListProtocol {

    public static void execute(JSONArray ja) {
        Whiteboard wb = Globals.getInstance().getWhiteboard();
        LinkedList<Buddy> buddies = new LinkedList<>();

        for(int i = 0; i < ja.length(); i++) {
            //Parse each buddy.
            try {
                JSONObject jo = ja.getJSONObject(i);
                String name = jo.getString("name");
                String email = jo.getString("clients");
                String picture64 = jo.getString("picture64");
                Buddy buddy = new Buddy(name, email, picture64, false);
                buddies.add(buddy);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e) {
                Log.e("BUDDYLISTPROTOCOL", "Malformed String");
            }
        }

        wb.setBuddies(buddies);
    }

}
