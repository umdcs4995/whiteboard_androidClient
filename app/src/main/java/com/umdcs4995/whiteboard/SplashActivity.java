package com.umdcs4995.whiteboard;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

/**
 * Activity handling the opening splash screen.
 */
public class SplashActivity extends AppCompatActivity {
    /**
     * Value for the time to show the splash screen (in milliseconds).
     */
    private final int DELAYTIME = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

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
        return new Intent(this, IntroActivity.class);
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
