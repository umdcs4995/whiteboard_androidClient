package com.umdcs4995.whiteboard;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.opengl.Visibility;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.NavigationView.OnNavigationItemSelectedListener;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi.DriveContentsResult;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFile.DownloadProgressListener;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.OpenFileActivityBuilder;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.Plus.PlusOptions;
import com.umdcs4995.whiteboard.driveOps.DriveLoadFragment;
import com.umdcs4995.whiteboard.driveOps.DriveSaveFragment;
import com.umdcs4995.whiteboard.services.SocketService;
import com.umdcs4995.whiteboard.uiElements.ContactListFragment;
import com.umdcs4995.whiteboard.uiElements.JoinBoardFragment;
import com.umdcs4995.whiteboard.uiElements.LoadURLFragment;
import com.umdcs4995.whiteboard.uiElements.LoadURLFragment.OnFragmentInteractionListener;
import com.umdcs4995.whiteboard.uiElements.LoadURLFragment.OnOkBtnClickedListener;
import com.umdcs4995.whiteboard.uiElements.LoginFragment;
import com.umdcs4995.whiteboard.uiElements.LoginFragment.GoogleSignInActivityResult;
import com.umdcs4995.whiteboard.uiElements.LoginFragment.OnLoginBtnClickedListener;
import com.umdcs4995.whiteboard.uiElements.SuicidalFragment;
import com.umdcs4995.whiteboard.uiElements.WhiteboardDrawFragment;
import com.umdcs4995.whiteboard.whiteboarddata.GoogleUser;
import com.umdcs4995.whiteboard.whiteboarddata.Whiteboard;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.UUID;

import io.socket.emitter.Emitter.Listener;


public class MainActivity extends AppCompatActivity implements OnNavigationItemSelectedListener,
        OnOkBtnClickedListener,
        OnFragmentInteractionListener, OnLoginBtnClickedListener, LoginFragment.OnFragmentInteractionListener, SuicidalFragment,
        ConnectionCallbacks, OnConnectionFailedListener {

    WhiteboardDrawFragment whiteboardDrawFragment = new WhiteboardDrawFragment();
    Fragment contactListFragment = new ContactListFragment();
    Fragment joinBoardFragment = new JoinBoardFragment();
    Fragment loadURLFragment = new LoadURLFragment();
    Fragment loginFragment = new LoginFragment();
    Fragment driveSaveFragment = new DriveSaveFragment();
    Fragment driveLoadFragment = new DriveLoadFragment();

    Fragment currentFragment = whiteboardDrawFragment;

    private SocketService socketService = Globals.getInstance().getSocketService();
    private GoogleSignInActivityResult pendingGoogleSigninResult;
    private static final int RC_SIGN_IN = 9001;
    private OnFragmentInteractionListener onFragmentInteractionListener;
    private GoogleApiClient googleApiClient;
    private GoogleSignInOptions gso;
    private String TAG = "MainActivity";
    /* Keys for persisting instance variables in savedInstanceState */
    private static final String KEY_IS_RESOLVING = "is_resolving";
    private static final String KEY_SHOULD_RESOLVE = "should_resolve";

    /* Is there a ConnectionResult resolution in progress? */
    private boolean mIsResolving = false;

    /* Should we automatically resolve ConnectionResults when possible? */
    private boolean mShouldResolve = false;

    private DriveId mSelectedFileDriveId;


    //Data members for the navigation view textboxes.  These need to be classwide to prevent
    //multiple inflations of the navbar header.
    private TextView tvNavHeaderName;
    private TextView tvNavHeaderEmail;
    private ImageView ivProfilePhoto;


    //Broadcast receiver instances.

    /**
     * Broadcast receiver for a reconnected event.
     */
    private BroadcastReceiver brReconnectionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("MAINACTIVITY", "Received reconnection broadcast.");
            Toast toast = Toast.makeText(getApplicationContext(), "Reconnected",
                    Toast.LENGTH_SHORT);
            toast.show();
        }
    };

    /**
     * Broadcast receiver for a connection lost event.
     */
    private BroadcastReceiver brConnectionLostReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("MAINACTIVITY", "Received connection lost broadcast.");
//            Toast toast = Toast.makeText(getApplicationContext(), "Connection Lost",
//                    Toast.LENGTH_SHORT);
//            toast.show();
            DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id
                    .drawer_layout);
            Snackbar snackbar = Snackbar
                    .make(drawerLayout, "Connection Lost.", Snackbar.LENGTH_INDEFINITE)
                    .setAction("RECONNECT", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Globals.getInstance().getSocketService().startReconnecting();
                        }
                    });
            snackbar.setActionTextColor(getResources().getColor(R.color.whiteboardBrightBlue));

            snackbar.show();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //SET THE TOOLBAR BELOW
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        //Check for an active Whiteboard and set the toolbar text to that Whiteboard.
        if(Globals.getInstance().getWhiteboard() != null) {
            toolbar.setTitle(Globals.getInstance().getWhiteboard().getWhiteboardName());
        } else {
            toolbar.setTitle("Whiteboard");
        }



        setSupportActionBar(toolbar);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        /**
         *Hides or makes visible the draw components and toolbar
         */
        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setImageDrawable(ContextCompat.getDrawable(this.getApplicationContext(), R.drawable.ic_viewpage));
        fab.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                whiteboardDrawFragment.fabHideMenu(fab);
                //Statement used to set toolbars visibility

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

        //Create the Google Api Client
        // Configure sign-in to request the user's ID, email address, and
        // basic profile.
        gso = new Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail().requestScopes(new Scope(Scopes.DRIVE_APPFOLDER), new Scope(Scopes.DRIVE_FILE), Drive.SCOPE_FILE, Plus.SCOPE_PLUS_LOGIN, Plus.SCOPE_PLUS_PROFILE).build();

        googleApiClient = new GoogleApiClient.Builder(this).enableAutoManage(this, this).addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .addApi(AppIndex.API).addApi(Drive.API).addConnectionCallbacks(this).addOnConnectionFailedListener(this)
                .addApi(Plus.API, PlusOptions.builder().build())
                //.addScope(Plus.SCOPE_PLUS_LOGIN)
                .build();

        if (googleApiClient.isConnected() == false) {
            googleApiClient.connect();
        }


        //Setup the textviews for manipulation whenever the user logs into a new account.
        //Need to be global to prevent multiple inflations.
        View headerView = navigationView.inflateHeaderView(R.layout.nav_header_main);
        tvNavHeaderName = (TextView) headerView.findViewById(R.id.navbar_username);
        tvNavHeaderEmail = (TextView) headerView.findViewById(R.id.navbar_email);
        ivProfilePhoto = (ImageView) headerView.findViewById(R.id.navbar_profilephoto);


        //Setup broadcast receiver
        registerBroadcastReceiver();

    }

    @Override
    public void onDestroy() {
        Globals.getInstance().stopSocketService();
        super.onDestroy();
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
    protected void onResume() {
        super.onResume();
        updateNavBarHeaderElements();
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

            // TODO: Change this to a generic "Whiteboards" button to create/join whiteboards
            // The client tries to join a whiteboard by sending the server the name of the whiteboard.
            // The server then replies with a error message or a join successful message.
            case R.id.join_board:
                if(currentFragment != joinBoardFragment) changeMainFragment(joinBoardFragment);
                break;

//            case R.id.nav_contacts://Navigates to list of contacts
//                changeMainFragment(contactListFragment);
//                break;

            case R.id.nav_settings://Navigates to Settings Activity
                Intent i = new Intent(this, SettingsActivity.class);
                startActivity(i);
                break;

            case R.id.add_url:
                if(currentFragment != loadURLFragment) changeMainFragment(loadURLFragment);
                break;

            case R.id.login:
                if(currentFragment != loginFragment) changeMainFragment(loginFragment);
                break;
            case R.id.google_drive:
                if(currentFragment != driveSaveFragment) {
                    Bundle bundle = new Bundle();
                    Bitmap b = findViewById(R.id.drawing).getDrawingCache();
                    ByteArrayOutputStream bs = new ByteArrayOutputStream();
                    b.compress(CompressFormat.PNG, 50, bs);
                    bundle.putByteArray("byteArray", bs.toByteArray());
                    driveSaveFragment.setArguments(bundle);
                    changeMainFragment(driveSaveFragment);
                }
                break;
            case R.id.addFile:
                if(currentFragment != driveLoadFragment) changeMainFragment(driveLoadFragment);
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

        if(!fragment.getClass().equals(whiteboardDrawFragment.getClass())) {
            //this will clear the back stack and displays no animation on the screen
            fm.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

            transaction.addToBackStack(fragment.toString());
        }

        transaction.replace(R.id.mainFrame, fragment);
        transaction.commit();


        currentFragment = fragment;
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
        changeMainFragment(whiteboardDrawFragment);
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
            pendingGoogleSigninResult = new GoogleSignInActivityResult(requestCode,
                    resultCode, data);
        }
        /**
         * A returned request code of 3 indicates that the intentSender has returned from the
         * DriveLoadFragment
         */
        if (requestCode == 3) {
            mSelectedFileDriveId = (DriveId) data.getParcelableExtra(
                    OpenFileActivityBuilder.EXTRA_RESPONSE_DRIVE_ID);

            open();
        }
    }



    /**
     * Support for GoogleApiClient.ConnectionCallbacks - After calling connect(), this method will
     * be invoked asynchronously when the connect request has successfully completed. After this
     * callback, the application can make requests on other methods provided by the client and
     * expect that no user intervention is required to call methods that use account and scopes
     * provided to the client constructor.
     */

    @Override
    public void onConnected(Bundle bundle) {
        // onConnected indicates that an account was selected on the device, that the selected account
        // has granted any requested permissions to our app and that we were able to establish a service
        // connection to Google Play services
        Log.d(TAG, "onConnected:" + bundle);
        mShouldResolve = false;
    }


    @Override
    public void onConnectionSuspended(int i) {
        // The connection to Google Play services was lost. The GoogleApiClient will automatically
        // attempt to re-connect. Any UI elements that depend on connection to Google APIs should be
        // hidden or disabled until onConnected is called again.
        Log.w(TAG, "onConnectionSuspended: " + i);
    }

    /**
     * Callback for GoogleApiClient connection failure
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.w(TAG, "onConnectionFailed: " + connectionResult);
        if (!mIsResolving && mShouldResolve) {
            if (connectionResult.hasResolution()) {
                try {
                    connectionResult.startResolutionForResult(this, RC_SIGN_IN);
                    mIsResolving = true;
                } catch (IntentSender.SendIntentException e) {
                    Log.e(TAG, "Could not resolve ConnectionResult", e);
                    mIsResolving = false;
                    googleApiClient.connect();
                }
            } else {
                // Could not resolve the connection result, show the user an error dialog
                showErrorDialog(connectionResult);
            }
        } else {
        }
    }

    private void showErrorDialog(ConnectionResult connectionResult) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this.getApplicationContext());

        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, RC_SIGN_IN,
                        new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                mShouldResolve = false;
                            }
                        }).show();
            } else {
                Log.w(TAG, "Google Play Services Error:" + connectionResult);
                mShouldResolve = false;
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // make sure to initiate a connection
        googleApiClient.connect();
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
        AppIndex.AppIndexApi.start(googleApiClient, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

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
        AppIndex.AppIndexApi.end(googleApiClient, viewAction);
        googleApiClient.disconnect();
    }

    public GoogleApiClient getGoogleApiClient() {
        return this.googleApiClient;
    }

    /**
     * Sets the title bar text of the activity
     */
    public void setTitleBarText(String s) {
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(s);
    }

    @Override
    public void onFragmentSuicide(int command) {
        if(command == this.POP_ME) {
            getSupportFragmentManager().popBackStack();
        }
    }

    /**
     * Toggles the visibility in the FAB button.  Can be called by fragments to toggle on or off
     * the FAB depending on their preferences.
     * @param isVisible
     */
    public void toggleFABVisibility(boolean isVisible) {

        if(isVisible) {
            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
            fab.show();
        } else {
            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
            fab.hide();
        }
    }

    /**
     * Gets the current draw mode from the WhiteboardDrawFragment
     */
    public boolean isDrawModeEnabled() {
        return whiteboardDrawFragment.getDrawMode();
    }

    private void open() {
        // Reset progress dialog back to zero as we're
        // initiating an opening request.
//        mProgressBar.setProgress(0);
//        DownloadProgressListener listener = new DownloadProgressListener() {
//            @Override
//            public void onProgress(long bytesDownloaded, long bytesExpected) {
//                // Update progress dialog with the latest progress.
//                int progress = (int) (bytesDownloaded * 100 / bytesExpected);
//                Log.d(TAG, String.format("Loading progress: %d percent", progress));
//                mProgressBar.setProgress(progress);
//                Intent in = new Intent(OpenFileActivity.this, EditContentsActivity.class);
//                startActivity(in);
//            }
//        };
        Log.d(TAG, "inside open");
        DriveFile driveFile = mSelectedFileDriveId.asDriveFile();
        driveFile.open(googleApiClient, DriveFile.MODE_READ_ONLY, new DownloadProgressListener() {
            @Override
            public void onProgress(long bytesDownloaded, long bytesExpected) {
                // Update progress dialog with the latest progress.
                int progress = (int) (bytesDownloaded * 100 / bytesExpected);
                Log.d(TAG, String.format("Loading progress: %d percent", progress));
            }
        })
                .setResultCallback(driveContentsCallback);
        mSelectedFileDriveId = null;
    }

    private ResultCallback<DriveContentsResult>
            driveContentsCallback = new ResultCallback<DriveContentsResult>() {
        public void onResult(DriveContentsResult result) {
            if (!result.getStatus().isSuccess()) {
                Log.d(TAG, "In result callback but can't open file");
                return;
            }
            Log.d(TAG, "Open File: file contents open");

            getSupportFragmentManager()
                    .beginTransaction()
                    .detach(driveLoadFragment)
                    .remove(driveLoadFragment)
                    .commit();


            // DriveContents object contains pointers to actual byte stream
            DriveContents contents = result.getDriveContents();
            Bitmap bitmap = null;
            InputStream is = contents.getInputStream();
            bitmap = BitmapFactory.decodeStream(is);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(CompressFormat.PNG, 100, stream);
            byte[] bytes = stream.toByteArray();

            Bundle bundle = new Bundle();
            bundle.putByteArray("image", bytes);
            whiteboardDrawFragment = new WhiteboardDrawFragment();
//            whiteboardDrawFragment.getArguments().putAll(bundle);
            whiteboardDrawFragment.setArguments(bundle);
            changeMainFragment(whiteboardDrawFragment);
        }

        ;
    };

    /**
     * Method for updating the navigation bar elements.  Should be called after the user logs
     * in or logs out.
     */
    public void updateNavBarHeaderElements() {
        GoogleUser gu = Globals.getInstance().getClientUser();

        if(gu.isLoggedIn()) {
            tvNavHeaderName.setText(gu.getFullname());
            tvNavHeaderEmail.setText(gu.getEmail());
            Bitmap b = gu.getRoundedProfileImage(70);
            ivProfilePhoto.setVisibility(View.INVISIBLE);
            if(b != null) {
                ivProfilePhoto.setImageBitmap(gu.getRoundedProfileImage(70));
            } else {
                ivProfilePhoto.setImageResource(R.drawable.whiteboard_logo);
            }

        } else {
            tvNavHeaderName.setText("Whiteboard");
            tvNavHeaderEmail.setText("Please Login");
            ivProfilePhoto.setImageResource(R.drawable.whiteboard_logo);
            ivProfilePhoto.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * This method registers the broadcast receivers neccessary for the main activity.
     */
    private void registerBroadcastReceiver() {
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(Globals.getInstance().getGlobalContext());

        //Register the intent receiver so that the view updates upon receiving.
        lbm.registerReceiver(brReconnectionReceiver,
                new IntentFilter(AppConstants.BM_RECONNECTED));

        lbm.registerReceiver(brConnectionLostReceiver,
                new IntentFilter(AppConstants.BM_CONNECTIONLOST));

    }


}


