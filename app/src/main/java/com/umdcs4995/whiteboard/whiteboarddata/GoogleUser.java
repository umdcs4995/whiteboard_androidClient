package com.umdcs4995.whiteboard.whiteboarddata;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.media.Image;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;

import com.umdcs4995.whiteboard.Globals;
import com.umdcs4995.whiteboard.services.ConnectivityException;
import com.umdcs4995.whiteboard.services.SocketService;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by rob on 4/18/16.
 */
public class GoogleUser {

    protected String fullname;
    protected String firstname;
    protected String lastname;
    protected String email;
    protected boolean loggedIn;
    protected Bitmap profilePhoto;
    protected String profilePhotoString;
    protected String profilePhotoURL;

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
        fullname = sp.getString("googleDisplayName", "");

        // Check to see that the full name exists.  If not, then the user hasn't logged in yet.
        // so set the flag and bail out.
        if (fullname == "") {
            loggedIn = false;
            return;
        }

        //First name is the first part of the full name up until the first space.
        firstname = fullname.substring(0, fullname.indexOf(" "));

        //Last name is the last part of the full name, starting at the first space.
        lastname = fullname.substring(fullname.indexOf(" "), fullname.length());


        //Get the email from the preferences
        email = sp.getString("googleUserEmail", "");

        //Get the picture
        profilePhotoString = sp.getString("googleDisplayPicture", "");
        profilePhoto = decodeBase64(sp.getString("googleDisplayPicture", ""));

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

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public String getEmail() {
        return email;
    }

    public Bitmap getImage() {
        return profilePhoto;
    }

    public String getProfileURL() {
        return profilePhotoURL;
    }

    public void setPRofileURL(String profileURL) {
        this.profilePhotoURL = profileURL;
    }

    // method for base64 to bitmap
    protected Bitmap decodeBase64(String input) {
        if(input.equals("")) {
            return null;
        } else {
            byte[] decodedByte = Base64.decode(input, 0);
            Log.d("GoogleUser", "Decoding image");
            return BitmapFactory
                    .decodeByteArray(decodedByte, 0, decodedByte.length);
        }
    }


    /**
     * Returns a rounded profile image bitmap for use in the navigation header.
     * @param radius
     * @return
     */
    public Bitmap getRoundedProfileImage(int radius) {
        if(profilePhoto == null) {
            //A profile image hasn't been saved.  Return null
            return null;
        }
        Bitmap bmp = profilePhoto;
        Bitmap sbmp;

        if (bmp.getWidth() != radius || bmp.getHeight() != radius) {
            float smallest = Math.min(bmp.getWidth(), bmp.getHeight());
            float factor = smallest / radius;
            sbmp = Bitmap.createScaledBitmap(bmp, (int)(bmp.getWidth() / factor), (int)(bmp.getHeight() / factor), false);
        } else {
            sbmp = bmp;
        }

        Bitmap output = Bitmap.createBitmap(radius, radius,
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xffa19774;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, radius, radius);

        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setDither(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(Color.parseColor("#BAB399"));
        canvas.drawCircle(radius / 2 + 0.7f,
                radius / 2 + 0.7f, radius / 2 + 0.1f, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(sbmp, rect, rect, paint);

        return output;
    }


    /**
     * Send the JSONObject to
     */
    public void sendInformationToUser() {

        SocketService ss = Globals.getInstance().getSocketService();

        JSONObject jo = new JSONObject();

        try {
            jo.put("name", fullname);
            jo.put("email", email);
            jo.put("picture", " ");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            ss.sendMessage(SocketService.Messages.CLIENTINFO, jo);
        } catch (ConnectivityException e) {
            //Do nothing here.  ConnectivityException handles reconnection.
        }
    }
}