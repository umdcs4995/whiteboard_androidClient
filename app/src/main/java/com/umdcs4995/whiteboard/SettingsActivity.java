package com.umdcs4995.whiteboard;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import uiFragments.SettingsFragment;

/**
 * Activity for handling the Settings menu displayed to the user upon selection.
 */
public class SettingsActivity extends AppCompatActivity
    implements SettingsFragment.OnFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) { //Set the title.
            getSupportActionBar().setTitle("Settings");
        }


    }

    /**
     * Part of the fragment interface.  Must be implemented.
     * @param uri
     */
    public void onFragmentInteraction(Uri uri) {
        //Do nothing here.
    }


}
