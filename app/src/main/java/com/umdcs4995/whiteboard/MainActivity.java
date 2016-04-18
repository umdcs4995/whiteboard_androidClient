package com.umdcs4995.whiteboard;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
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
import com.umdcs4995.whiteboard.driveOps.DriveLoadFragment;
import com.umdcs4995.whiteboard.driveOps.DriveSaveFragment;
import com.umdcs4995.whiteboard.services.SocketService;
import com.umdcs4995.whiteboard.services.SocketService.Messages;
import com.umdcs4995.whiteboard.uiElements.ContactListFragment;
import com.umdcs4995.whiteboard.uiElements.JoinBoardFragment;
import com.umdcs4995.whiteboard.uiElements.LoadURLFragment;
import com.umdcs4995.whiteboard.uiElements.LoadURLFragment.OnFragmentInteractionListener;
import com.umdcs4995.whiteboard.uiElements.LoadURLFragment.OnOkBtnClickedListener;
import com.umdcs4995.whiteboard.uiElements.LoginFragment;
import com.umdcs4995.whiteboard.uiElements.LoginFragment.GoogleSignInActivityResult;
import com.umdcs4995.whiteboard.uiElements.LoginFragment.OnLoginBtnClickedListener;
import com.umdcs4995.whiteboard.uiElements.NewBoardFragment;
import com.umdcs4995.whiteboard.uiElements.WhiteboardDrawFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.UUID;

import io.socket.emitter.Emitter.Listener;

//import com.umdcs4995.whiteboard.uiElements.LoginFragment.OnLoginBtnClickedListener;



public class MainActivity extends AppCompatActivity
        implements OnNavigationItemSelectedListener,
        OnOkBtnClickedListener,
        OnFragmentInteractionListener, OnLoginBtnClickedListener, LoginFragment.OnFragmentInteractionListener,
        ConnectionCallbacks, OnConnectionFailedListener {

    Fragment whiteboardDrawFragment = new WhiteboardDrawFragment();
    Fragment contactListFragment = new ContactListFragment();
    Fragment joinBoardFragment = new JoinBoardFragment();
    Fragment newBoardFragment = new NewBoardFragment();
    Fragment loadURLFragment = new LoadURLFragment();
    Fragment loginFragment = new LoginFragment();
    Fragment driveSaveFragment = new DriveSaveFragment();
    Fragment driveLoadFragment = new DriveLoadFragment();

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

        //Create the Google Api Client
        // Configure sign-in to request the user's ID, email address, and
        // basic profile.
        gso = new Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail().requestScopes(new Scope(Scopes.DRIVE_APPFOLDER), Drive.SCOPE_FILE).build();

        googleApiClient = new GoogleApiClient.Builder(this).enableAutoManage(this, this).addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .addApi(AppIndex.API).addApi(Drive.API).addConnectionCallbacks(this).addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .build();

        if (googleApiClient.isConnected() == false) {
            googleApiClient.connect();
        }
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
                    String uuid = UUID.randomUUID().toString();
                    createWbRequest.put("name", uuid);
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
                changeMainFragment(loadURLFragment);
                break;

            case R.id.login:
                changeMainFragment(loginFragment);
                break;
            case R.id.google_drive:
                Bundle bundle = new Bundle();
                Bitmap b = findViewById(R.id.drawing).getDrawingCache();
                ByteArrayOutputStream bs = new ByteArrayOutputStream();
                b.compress(CompressFormat.PNG, 50, bs);
                bundle.putByteArray("byteArray", bs.toByteArray());
                driveSaveFragment.setArguments(bundle);
                changeMainFragment(driveSaveFragment);
                break;
            case R.id.addFile:
                changeMainFragment(driveLoadFragment);
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
        if (requestCode == 3) {
            Log.d(TAG, "received intent sender from drive load?");
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
                String errorString = apiAvailability.getErrorString(resultCode);
                //Toast.makeText(this, errorString, Toast.LENGTH_SHORT).show();

                mShouldResolve = false;
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
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
                //mProgressBar.setProgress(progress);
            }
        })
                .setResultCallback(driveContentsCallback);
        mSelectedFileDriveId = null;
        //finish();
    }

    private ResultCallback<DriveContentsResult>
            driveContentsCallback = new ResultCallback<DriveContentsResult>() {
        public void onResult(DriveContentsResult result) {
            if (!result.getStatus().isSuccess()) {
                Log.d(TAG, "In result callback but can't open file");
                return;
            }
            //showMessage("Open File: file contents open");
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
            // new here
            byte[] bytes = stream.toByteArray();
            //Need to find the stuff that used to be in main that would handle the data and
            //put it instead into an onFinish() listener of some type that we initalize here
            //as an interface.
//            Intent resultIntent = new Intent(MainActivity.this, WhiteboardDrawFragment.class);
//            resultIntent.putExtra("image", bytes);
//            setResult(Activity.RESULT_OK, resultIntent);
//            Log.d(TAG, "made it to just before finish");
//            startActivity(resultIntent);
            Bundle bundle = new Bundle();
            bundle.putByteArray("image", bytes);
            whiteboardDrawFragment = new WhiteboardDrawFragment();
//            whiteboardDrawFragment.getArguments().putAll(bundle);
            whiteboardDrawFragment.setArguments(bundle);
            changeMainFragment(whiteboardDrawFragment);

//            DrawingView drawingView = (DrawingView) findViewById(R.id.drawing);
//            drawingView.setCanvasBitmap(bitmap);
//            Drawable drawBitMap = new BitmapDrawable(bitmap);
            //drawingView.setBackground(drawBitMap);
//            changeMainFragment(whiteboardDrawFragment);


            //finish();
            //Log.d(TAG, "somehow after finish");
        }

        ;
    };

}
