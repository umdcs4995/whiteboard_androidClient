package com.umdcs4995.whiteboard.protocol;

import android.util.Log;

import com.umdcs4995.whiteboard.Globals;
import com.umdcs4995.whiteboard.whiteboarddata.GoogleUser;
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

    public static void execute(JSONObject incomingList) {
        Whiteboard wb = Globals.getInstance().getWhiteboard();
        LinkedList<GoogleUser> buddies = new LinkedList<>();

        JSONArray ja = null;
        try {
            ja = incomingList.getJSONArray("clients");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        for(int i = 0; i < ja.length(); i++) {
            //Parse each buddy.
            try {
                JSONObject jo = ja.getJSONObject(i);
                String name = jo.getString("name");
                String email = jo.getString("email");
                String picture64 = jo.getString("picture");
                GoogleUser buddy = new GoogleUser(name, email, picture64, false);
                buddies.add(buddy);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e) {
                Log.e("BUDDYLISTPROTOCOL", "Malformed String");
                e.printStackTrace();
            }
        }

        wb.setBuddies(buddies);
    }

}
