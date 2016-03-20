package com.umdcs4995.whiteboard;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

/**
 * Activity handling the opening splash screen.
 */
public class SplashActivity extends AppCompatActivity {
    /**
     * Value for the time to show the splash screen (in milliseconds).
     */
    private final int DELAYTIME = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        TextView tv = (TextView) findViewById(R.id.txtWelcome);


        //Code to read from shared preferences!
        //Read from a key value pair.
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        //Extract the resource
        String username = sp.getString("pref_name","");

        if (username.length() > 0) {
            tv.setText("Welcome, " + username);
        } else {
            tv.setText("Welcome!");
        }


        /**
         * Post delayed event is fired after DELAYTIME has expired.  This method is called as soon
         * as the splash screen is rendered.  launchActivity() returns an object of type Runnable,
         * whose run() method is overridden (see private methods).
         */
        new Handler().postDelayed(launchActivity(), DELAYTIME);
    }


    /**
     * Method returns an intent to begin the intro activity.  A private method is required to
     * get a "this" reference to the current activity, since the intent is begun in a runnable type
     * object.
     * @return Intent to execute the IntroActivity.
     */
    private Intent makeMainIntent() {
        return new Intent(this, MasterWhiteboardAddActivity.class);
    }


    /**
     * This method is exectued on creation of the splash screen after DELAYTIME ms has passed.
     * @return a runnable to execute.
     */
    private Runnable launchActivity() {
        return new Runnable() {
            @Override
            public void run() {
                //Creates an intent to the next activity.
                Intent in = makeMainIntent();
                //Fire that intent.
                startActivity(in);
            }
        };
    }

}
