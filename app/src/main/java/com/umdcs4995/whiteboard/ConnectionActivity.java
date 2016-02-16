package com.umdcs4995.whiteboard;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import xmpp_client.XMPP;

/**
 * Activity for handling the XMPP connection to the Openfire server.
 */

public class ConnectionActivity extends AppCompatActivity {

    /**
     * The class housing all XMPP functionality.
     */
    private XMPP xm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        /**
         * @param "45.55.183.45" a String representation of the server address.
         */
        xm = new XMPP("45.55.183.45", "hcc", "adminpassword");
        xm.connect();
    }

}
