package com.umdcs4995.whiteboard.whiteboarddata;

/**
 * A subclass of user which is just a buddy.  Used for the buddy list.
 * Created by Rob on 4/25/2016.
 */
public class Buddy extends GoogleUser {

    /**
     * Creates a new buddy object.  The face64 string will be converted into a bitmap using
     * the superclass' method.
     */
    public Buddy(String fullname, String email, String face64, boolean loggedIn) {
        super.fullname = fullname;
        super.email = email;
        super.profilePhoto = super.decodeBase64(face64);

        //First name is the first part of the full name up until the first space.
        super.firstname = fullname.substring(0, fullname.indexOf(" "));

        //Last name is the last part of the full name, starting at the first space.
        super.lastname = fullname.substring(fullname.indexOf(" "), fullname.length());

        super.loggedIn = loggedIn;
    }

}
