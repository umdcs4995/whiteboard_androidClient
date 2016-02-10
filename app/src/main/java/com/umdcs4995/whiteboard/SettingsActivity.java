package com.umdcs4995.whiteboard;

import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import uiFragments.SettingsFragment;

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
