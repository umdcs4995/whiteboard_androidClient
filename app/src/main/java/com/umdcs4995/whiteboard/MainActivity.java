package com.umdcs4995.whiteboard;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.umdcs4995.whiteboard.services.SocketService;
import com.umdcs4995.whiteboard.uiElements.ContactListFragment;
import com.umdcs4995.whiteboard.uiElements.JoinBoardFragment;
import com.umdcs4995.whiteboard.uiElements.LoadURLFragment;
import com.umdcs4995.whiteboard.uiElements.WhiteboardDrawFragment;

import org.json.JSONException;
import org.json.JSONObject;

import io.socket.emitter.Emitter;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        LoadURLFragment.OnOkBtnClickedListener,
        LoadURLFragment.OnFragmentInteractionListener{

    Fragment whiteboardDrawFragment = new WhiteboardDrawFragment();
    Fragment contactListFragment = new ContactListFragment();
    Fragment joinBoardFragment = new JoinBoardFragment();
    Fragment loadURLFragment = new LoadURLFragment();

    private SocketService socketService = Globals.getInstance().getSocketService();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //SET THE TOOLBAR BELOW
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Whiteboard");
        setSupportActionBar(toolbar);

        //SET THE FLOATING ACTION BELOW
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WhiteboardDrawFragment.fabHideMenu(view);
            }
        });

        //SET THE NAVIGATION DRAWER BELOW
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //SETUP THE DEFAULT FRAGMENT
        changeMainFragment(whiteboardDrawFragment);

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }


        return super.onOptionsItemSelected(item);
    }


    /**
     * NavItem selected method
     * @param item
     * @return
     */
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id) {

            case R.id.add_board:
                JSONObject createWbRequest = new JSONObject();
                try {
                    // TODO: make a whiteboard name chooser and use its input here
                    createWbRequest.put("name", "replace_me");
                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(), "Error making createWhiteboard request - this is bad...", Toast.LENGTH_LONG);
                }
                socketService.sendMessage(SocketService.Messages.CREATE_WHITEBOARD, createWbRequest);

                socketService.addListener(SocketService.Messages.CREATE_WHITEBOARD, new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        // TODO: Set up the whiteboard + join it here
                        JSONObject recvd = (JSONObject)args[0];
                        try {
                            Log.i("createWhiteboard", "received message: " + recvd.getString("message"));
                        } catch (JSONException e) {
                            Log.w("createWhiteboard", "error parsing received message");
                        }
                        socketService.clearListener(SocketService.Messages.CREATE_WHITEBOARD);
                    }
                });
                break;

            case R.id.join_board:
                changeMainFragment(joinBoardFragment);
                break;

            case R.id.nav_contacts:
                changeMainFragment(contactListFragment);
                break;

            case R.id.nav_settings:
                Intent i = new Intent(this, SettingsActivity.class);
                startActivity(i);
                break;

            case R.id.add_url:
                //loadURLFragment.startActivity(new Intent(this, LoadURLFragment.class));
                changeMainFragment(loadURLFragment);
                break;

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void changeMainFragment(Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(R.id.mainFrame, fragment);
        transaction.addToBackStack(fragment.toString());
        transaction.commit();
    }

    @Override
    public void onOkBtnClicked(String urlString) {
        WhiteboardDrawFragment tempFragment = (WhiteboardDrawFragment) whiteboardDrawFragment;
       tempFragment.setNewBackground(urlString);
        changeMainFragment(whiteboardDrawFragment);

    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
