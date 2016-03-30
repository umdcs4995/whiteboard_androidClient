package com.umdcs4995.whiteboard;

import android.app.ProgressDialog;
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

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.services.gmail.GmailScopes;
import com.umdcs4995.whiteboard.services.SocketService;
import com.umdcs4995.whiteboard.services.SocketService.Messages;
import com.umdcs4995.whiteboard.uiElements.ContactListFragment;
import com.umdcs4995.whiteboard.uiElements.JoinBoardFragment;
import com.umdcs4995.whiteboard.uiElements.LoadURLFragment;
import com.umdcs4995.whiteboard.uiElements.LoadURLFragment.OnFragmentInteractionListener;
import com.umdcs4995.whiteboard.uiElements.LoadURLFragment.OnOkBtnClickedListener;
import com.umdcs4995.whiteboard.uiElements.LoginFragment;
import com.umdcs4995.whiteboard.uiElements.LoginFragment.OnLoginBtnClickedListener;
import com.umdcs4995.whiteboard.uiElements.WhiteboardDrawFragment;

import org.json.JSONException;
import org.json.JSONObject;

import io.socket.emitter.Emitter.Listener;

public class MainActivity extends AppCompatActivity
        implements OnNavigationItemSelectedListener,
        OnOkBtnClickedListener,
        OnFragmentInteractionListener, OnLoginBtnClickedListener, GoogleApiClient.ConnectionCallbacks {

    Fragment whiteboardDrawFragment = new WhiteboardDrawFragment();
    Fragment contactListFragment = new ContactListFragment();
    Fragment joinBoardFragment = new JoinBoardFragment();
    Fragment loadURLFragment = new LoadURLFragment();
    Fragment loginFragment = new LoginFragment();

    private SocketService socketService = Globals.getInstance().getSocketService();

    private GoogleApiClient client, googleApiClient;
    private GoogleSignInOptions gso;
    private GoogleAccountCredential googleAccountCredential;
    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = {GmailScopes.GMAIL_LABELS};
    private static final String TAG = "MainActivity";
    private static final int RC_SIGN_IN = 9001;
    private ProgressDialog progressDialog;




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
        fab.setOnClickListener(new OnClickListener() {
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

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        //client = new Builder(this).addApi(AppIndex.API).build();
//        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                .requestEmail().requestScopes(new Scope(Scopes.DRIVE_APPFOLDER)).build();

        // Build a GoogleAPIClient with access to the Google Sign-in api and
        // the other options specified above by the gso.
//        googleApiClient = new GoogleApiClient.Builder(this)
//                .enableAutoManage(this, (OnConnectionFailedListener) this)
//                .addApi(Auth.GOOGLE_SIGN_IN_API, gso).addApi(AppIndex.API).build();
//        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
//        googleAccountCredential = GoogleAccountCredential.usingOAuth2(getApplicationContext(), Arrays.asList(SCOPES)).setBackOff(new ExponentialBackOff()).setSelectedAccountName(settings.getString(PREF_ACCOUNT_NAME, null));

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

//            case R.id.login:
//                Intent in = new Intent(this, LoginFragment.class);
//                changeMainFragment(loginFragment);
//                break;

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
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            //Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            //mStatusTextView.setText(getString(R.string.signed_in_fmt, acct.getDisplayName()));
            updateUI(true);

        } else {
            //Signed Out, show unathenticated UI.
            updateUI(false);
        }
    }

    private void updateUI(boolean signedIn) {
        if (signedIn) {
            findViewById(R.id.sign_in_button).setVisibility(View.GONE);

        } else {
            //mStatusTextView.setText(R.string.signed_out);
            findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
        }
    }

    public void onConnectionFailed(ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
//        googleApiClient.connect();
//        Action viewAction = Action.newAction(
//                Action.TYPE_VIEW, // TODO: choose an action type.
//                "Main Page", // TODO: Define a title for the content shown.
//                // TODO: If you have web page content that matches this app activity's content,
//                // make sure this auto-generated web page URL is correct.
//                // Otherwise, set the URL to null.
//                Uri.parse("http://host/path"),
//                // TODO: Make sure this auto-generated app deep link URI is correct.
//                Uri.parse("android-app://com.umdcs4995.whiteboard/http/host/path")
//        );
//        AppIndex.AppIndexApi.start(googleApiClient, viewAction);
//
//        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(googleApiClient);
//        if (opr.isDone()) {
//            // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
//            // and the GoogleSignInResult will be available instantly.
//            Log.d(TAG, "Got cached sign-in");
//            GoogleSignInResult result = opr.get();
//            handleSignInResult(result);
//        } else {
//            // If the user has not previously signed in on this device or the sign-in has expired,
//            // this asynchronous branch will attempt to sign in the user silently.  Cross-device
//            // single sign-on will occur in this branch.
//            showProgressDialog();
//            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
//                @Override
//                public void onResult(GoogleSignInResult googleSignInResult) {
//                    hideProgressDialog();
//                    handleSignInResult(googleSignInResult);
//                }
//            });
//        }
    }

    @Override
    public void onStop() {
        super.onStop();

        //disconnect api if it is connected
        if (googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.umdcs4995.whiteboard/http/host/path")
        );
//        AppIndex.AppIndexApi.end(googleApiClient, viewAction);
//        googleApiClient.disconnect();
    }

    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            //progressDialog.setMessage(getString(R.string.loading));
            progressDialog.setIndeterminate(true);
        }
        progressDialog.show();
    }

    private void hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.hide();
        }
    }

    /**
     * Callback for GoogleApiClient connection success
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        //mSignInClicked = false;
        Toast.makeText(getApplicationContext(), "Signed In Successfully",
                Toast.LENGTH_LONG).show();

        //processUserInfoAndUpdateUI();

        updateUI(true);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }
}
