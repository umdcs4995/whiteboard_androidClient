package com.umdcs4995.whiteboard;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.NavigationView.OnNavigationItemSelectedListener;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.umdcs4995.whiteboard.services.SocketService;
import com.umdcs4995.whiteboard.services.SocketService.Messages;
import com.umdcs4995.whiteboard.uiElements.ContactListFragment;
import com.umdcs4995.whiteboard.uiElements.JoinBoardFragment;
import com.umdcs4995.whiteboard.uiElements.LoadURLFragment;
import com.umdcs4995.whiteboard.uiElements.LoadURLFragment.OnFragmentInteractionListener;
import com.umdcs4995.whiteboard.uiElements.LoadURLFragment.OnOkBtnClickedListener;
import com.umdcs4995.whiteboard.uiElements.LoginFragment;
import com.umdcs4995.whiteboard.uiElements.LoginFragment.OnLoginBtnClickedListener;
import com.umdcs4995.whiteboard.uiElements.NewBoardFragment;
import com.umdcs4995.whiteboard.uiElements.WhiteboardDrawFragment;

import org.json.JSONException;
import org.json.JSONObject;

import io.socket.emitter.Emitter.Listener;

//import com.umdcs4995.whiteboard.uiElements.LoginFragment.OnLoginBtnClickedListener;



public class MainActivity extends AppCompatActivity
        implements OnNavigationItemSelectedListener,
        OnOkBtnClickedListener,
        OnFragmentInteractionListener, OnLoginBtnClickedListener, LoginFragment.OnFragmentInteractionListener
        /*ConnectionCallbacks, OnConnectionFailedListener */{

    Fragment whiteboardDrawFragment = new WhiteboardDrawFragment();
    Fragment contactListFragment = new ContactListFragment();
    Fragment joinBoardFragment = new JoinBoardFragment();
    Fragment newBoardFragment = new NewBoardFragment();
    Fragment loadURLFragment = new LoadURLFragment();
    Fragment loginFragment = new LoginFragment();

    private SocketService socketService = Globals.getInstance().getSocketService();

    private LoginFragment.GoogleSignInActivityResult pendingGoogleSigninResult;
    private static final String TAG = "MainActivity";
    private static final int RC_SIGN_IN = 9001;
    private boolean isSignedIn;
    private OnFragmentInteractionListener onFragmentInteractionListener;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //SET THE TOOLBAR BELOW
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Whiteboard");
        setSupportActionBar(toolbar);

        /**
         *Hides or makes visible the draw components and toolbar
         */
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                WhiteboardDrawFragment.fabHideMenu(view);//Function used to set draw components visibility
                //Statement used to set toolbars visibility
                if(toolbar.getVisibility()==view.GONE){
                    toolbar.setVisibility(view.VISIBLE);
                }
                else{
                    toolbar.setVisibility(view.GONE);
                }
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

    /*
     * This function handles the back button closing the navigation drawer and
     * then calling the parent back pressed function
     */
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
     *
     * @param item
     * @return
     */
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id) {

            // TODO: move to NewBoardFragment class and call changeMainFragment()
            // The client tries to create new whiteboard by sending the server the name of the whiteboard.
            // The server then replies with a error message or a create successful message.
            case R.id.add_board:
                JSONObject createWbRequest = new JSONObject();
                try {
                    // TODO: make a whiteboard name chooser and use its input here
                    createWbRequest.put("name", "replace_me");
                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(), "Error making createWhiteboard request - this is bad...", Toast.LENGTH_LONG);
                }
                socketService.sendMessage(Messages.CREATE_WHITEBOARD, createWbRequest);

                socketService.addListener(Messages.CREATE_WHITEBOARD, new Listener() {
                    @Override
                    public void call(Object... args) {
                        // TODO: Set up the whiteboard + join it here
                        JSONObject recvd = (JSONObject) args[0];
                        try {
                            Log.i("createWhiteboard", "received message: " + recvd.getString("message"));
                        } catch (JSONException e) {
                            Log.w("createWhiteboard", "error parsing received message");
                        }
                        socketService.clearListener(Messages.CREATE_WHITEBOARD);
                    }
                });
                break;

            // The client tries to join a whiteboard by sending the server the name of the whiteboard.
            // The server then replies with a error message or a join successful message.
            case R.id.join_board:
                changeMainFragment(joinBoardFragment);
                break;

            case R.id.nav_contacts://Navigates to list of contacts
                changeMainFragment(contactListFragment);
                break;

            case R.id.nav_settings://Navigates to Settings Activity
                Intent i = new Intent(this, SettingsActivity.class);
                startActivity(i);
                break;

            case R.id.add_url:
                //loadURLFragment.startActivity(new Intent(this, LoadURLFragment.class));
                changeMainFragment(loadURLFragment);
                break;

            case R.id.login:
//                Intent in = new Intent(this, LoginFragment.class);
                changeMainFragment(loginFragment);
//                signIn();
                break;

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Replaces the current fragment on the mainFrame layout.
     * @param fragment
     */
    private void changeMainFragment(Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(R.id.mainFrame, fragment);
        transaction.addToBackStack(fragment.toString());
        transaction.commit();
    }

    /*
     * This function handles the "ok" button for loading images from a URL
     * it creates a temporary fragment and sets the new background to the
     * specified url and changes the fragment to the new one
     */
    @Override
    public void onOkBtnClicked(String urlString) {
        WhiteboardDrawFragment tempFragment = (WhiteboardDrawFragment) whiteboardDrawFragment;
        //tempFragment.setNewBackground(urlString);
       tempFragment.loadBackgroundFromURL(urlString);
        changeMainFragment(whiteboardDrawFragment);

    }

    @Override
    public void onLoginBtnClicked() {
        WhiteboardDrawFragment tempFragment = (WhiteboardDrawFragment) whiteboardDrawFragment;
        changeMainFragment(whiteboardDrawFragment);
        //tempFragment.
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        // LoginFragment doesn't get all of the onActivityResults for google sign in
        // so the activity needs to proxy them through but only after the LoginFragment has
        // been registered with the event bus.
        if (requestCode == RC_SIGN_IN) {
//            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            pendingGoogleSigninResult = new LoginFragment.GoogleSignInActivityResult(requestCode,
                    resultCode, data);
            //handleSignInResult(result);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            //Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            isSignedIn = true;
            //mStatusTextView.setText(getString(R.string.signed_in_fmt, acct.getDisplayName()));
            //updateUI(true);

        } else {
            //Signed Out, show unathenticated UI.
            isSignedIn = false;
        }
    }

//    private void signIn() {
//        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
//        startActivityForResult(signInIntent, RC_SIGN_IN);
//    }


    public boolean openLoginDialogIfLoggedOut() {
        if (!isSignedIn) {
            //LoginFragment.newInstance().show(getSupportFragmentManager(), "LoginFragment");
            return true;
        } else {
            return false;
        }
    }

    public int getActivityid() {
        return 0;
    }
}
