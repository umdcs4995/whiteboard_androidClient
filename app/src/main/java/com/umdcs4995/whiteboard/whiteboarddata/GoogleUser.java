package com.umdcs4995.whiteboard.whiteboarddata;

import android.content.SharedPreferences;
import android.media.Image;
import android.preference.PreferenceManager;

import com.umdcs4995.whiteboard.Globals;

/**
 * Created by rob on 4/18/16.
 */
public class GoogleUser {

    private String fullname;
    private String firstname;
    private String lastname;
    private String email;
    private Image face;
    private String faceBase64;
    private boolean googleUser;
    private boolean loggedIn;

    /**
     * Constructor for the GoogleUser.  Note the current fields are populated by a method call
     * which requires the global application context from Globals, so don't call this until
     * AFTER Globals has been initiated.
     */
    public GoogleUser() {
        populateFromCache();
    }


    /**
     * This method accesses items in the SharedPreferences that should be stored after the
     * user logs in using the Google APIs.
     */
    private void populateFromCache() {
        //Code to read from shared preferences!
        //Read from a key value pair.
        SharedPreferences sp = PreferenceManager.
                getDefaultSharedPreferences(Globals.getInstance().getGlobalContext());
        //Extract the resource
        fullname = sp.getString("googleDisplayName","");

        // Check to see that the full name exists.  If not, then the user hasn't logged in yet.
        // so set the flag and bail out.
        if(fullname == "") {
            loggedIn = false;
            return;
        }

        //First name is the first part of the full name up until the first space.
        firstname = fullname.substring(0, fullname.indexOf(" "));

        //Last name is the last part of the full name, starting at the first space.
        lastname = fullname.substring(fullname.indexOf(" "), fullname.length());


        //Get the email from the preferences
        email = sp.getString("googleUserEmail", "");

        //Set the flag.
        loggedIn = true;
    }





    //======Getters and Setters===========
    public String getLastname() {
        return lastname;
    }

    public String getFullname() {
        return fullname;
    }

    public String getFirstname() {
        return firstname;
    }

    public Image getFace() {
        return face;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public String getEmail() {
        return email;
    }
}
