package com.umdcs4995.whiteboard;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import uiFragments.AboutDialogFragment;
import uiFragments.NotYetImplementedToast;

public class HostWhiteBoardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host_white_board);

        //Create the toolbar instance here.  The toolbar is the thing at the top with the name
        //and overflow menu.  Sets the title.
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) { //Set the title.
            getSupportActionBar().setTitle("Whiteboard");
        }


        //Floating Action button stuff listed below.
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            //Set the listener for when the button is pressed
            @Override
            public void onClick(View view) {
                //Toast the user here, saying button was pressed.
                Toast toast = Toast.makeText(getApplicationContext(), "You've Been Called On.",
                        Toast.LENGTH_SHORT);
                toast.show();
                startActivity(makeAddIntent());
            }
        });


    }

    /**
     * This method override creates the settings menu (the three dots in the upper right corner.
     * It's called automatically after onCreate(..).  To see the xml code for the menu, see the
     * menu_mainoverflow.xml file.
     */
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_mainoverflow, menu);
        return true;
    }

    /**
     * This method handles menu item presses.  It implements a case-switch system against the ID
     * for the MenuItem pressed.
     */
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menuitem_settings:
                NotYetImplementedToast toast = new NotYetImplementedToast(
                        this.getApplicationContext(), "Settings menu item pressed.");
                return true;
            case R.id.menuitem_about:
                AboutDialogFragment about = new AboutDialogFragment();
                about.show(getFragmentManager(),"About");
                return true;
            default:
                return false;
        }
    }

    private Intent makeAddIntent() {
        return new Intent(this, MasterWhiteboardAddActivity.class);
    }
}
