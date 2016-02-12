package com.umdcs4995.whiteboard;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import uiFragments.AboutDialogFragment;
import uiFragments.NotYetImplementedToast;

/**
 * Activity for handling the introduction screen for the app.  It contains the buttons for launching
 * various activities as directed by the user.
 */
public class IntroActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//
//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
    }

    /**
     * This method executes when the New White Board Acticity button is pressed.  It is linked to
     * the button in the XML file.  Notice how android:onClick="onClickButtonNew" is a parameter
     * for the button in the XML.  Android allows linking buttons in that way, which avoids the
     * extra code final Button b = ..., b.setOnClickListener(...
     *
     * Starts a new HostWhiteBoardActivity.
     */
    public void onClickButtonNew(View view) {
        Intent in = new Intent(this, HostWhiteBoardActivity.class);
        startActivity(in);
    }

    /**
     * Method executed when the Join Whiteboard button is clicked.
     * @param view
     */
    public void onClickButtonJoin(View view) {
        new NotYetImplementedToast(this.getApplicationContext());
    }

    /**
     * Method executed when the Load Whiteboard button is clicked.
     * @param view
     */
    public void onClickButtonLoad(View view) {
        Intent in = new Intent(this, MasterWhiteboardAddActivity.class);
        startActivity(in);
    }

    /**
     * Executed on the click of the Settings button.  It launches a settings activity.
     */
    public void onSettingsClick(View view) {
        Intent in = new Intent(this, HostWhiteBoardActivity.class);
        startActivity(in);
    }


    /**
     * Executed on the click of the About button.  It launches an "About Game" dialog.
     */
    public void onAboutClick(View view) {
        //Launch About game dialog box.
        AboutDialogFragment about = new AboutDialogFragment();
        about.show(getFragmentManager(),"About");
    }
}
