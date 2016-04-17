package com.umdcs4995.whiteboard.uiElements;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.gmail.GmailScopes;
import com.umdcs4995.whiteboard.R;

import java.util.Arrays;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link LoginFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link LoginFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 * Created by Laura 3/29/16
 */
public class LoginFragment extends Fragment implements GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener, GoogleApiClient.ConnectionCallbacks {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = "LoginFragment";

    private static final int REQUEST_SIGNUP = 0;
    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;

    /* RequestCode for resolutions to get GET_ACCOUNTS permission on M */
    private static final int RC_PERM_GET_ACCOUNTS = 2;

    /* RequestCode for resolutions involving sign-in */
    private static final int RC_SIGN_IN = 9001;
    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = {GmailScopes.GMAIL_LABELS, Scopes.PROFILE, Scopes.EMAIL};

    private GoogleAccountCredential credential;

    /* Client for accessing Google APIs */
    private GoogleApiClient googleApiClient = null;
    private SignInButton signInButton;

    /* Keys for persisting instance variables in savedInstanceState */
    private static final String KEY_IS_RESOLVING = "is_resolving";
    private static final String KEY_SHOULD_RESOLVE = "should_resolve";

    /* Is there a ConnectionResult resolution in progress? */
    private boolean mIsResolving = false;

    /* Should we automatically resolve ConnectionResults when possible? */
    private boolean mShouldResolve = false;

    /* View to display current status (signed-in, out) */
    private TextView statusTextView;
    private ProgressDialog progressDialog;
    private View loginView;
    private GoogleSignInOptions gso;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener listener;
    private OnLoginBtnClickedListener loginBtnClickedListener;

    private Uri personPhoto;
    private ImageView profileImg;


    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public void onConnected(Bundle bundle) {
        // onConnected indicates that an account was selected on the device, that the selected account
        // has granted any requested permissions to our app and that we were able to establish a service
        // connection to Google Play services
        Log.d(TAG, "onConnected:" + bundle);
        mShouldResolve = false;
        updateUI(true);
    }

    @Override
    public void onConnectionSuspended(int i) {
        // The connection to Google Play services was lost. The GoogleApiClient will automatically
        // attempt to re-connect. Any UI elements that depend on connection to Google APIs should be
        // hidden or disabled until onConnected is called again
        Log.w(TAG, "onConnectionSuspended: " + i);
    }

    public interface OnLoginBtnClickedListener {
        public void onLoginBtnClicked();
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment LoginFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LoginFragment newInstance() {
        LoginFragment fragment = new LoginFragment();
        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "at least made it to login frag");
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        // Configure sign-in to request the user's ID, email address, and
        // basic profile.
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail().requestScopes(new Scope(Scopes.DRIVE_APPFOLDER)).build();

        // Build a GoogleAPIClient with access to the Google Sign-in api and
        // the other options specified above by the gso.
        Context context = getActivity();
        googleApiClient = new GoogleApiClient.Builder(getActivity())
            .enableAutoManage(getActivity(), this)
            .addApi(Auth.GOOGLE_SIGN_IN_API, gso).addApi(AppIndex.API)
                .addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(Plus.API).build();
        //loginView.findViewById(R.id.sign_in_button).setOnClickListener(this);
        SharedPreferences settings = getActivity().getPreferences(Context.MODE_PRIVATE);
        credential = GoogleAccountCredential.usingOAuth2(getActivity().getApplicationContext(), Arrays.asList(SCOPES)).setBackOff(new ExponentialBackOff()).setSelectedAccountName(settings.getString(PREF_ACCOUNT_NAME, null));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        loginView = inflater.inflate(R.layout.fragment_login, container, false);
        //Configure the sign in button
        signInButton = (SignInButton) loginView.findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        signInButton.setScopes(gso.getScopeArray());
        statusTextView = (TextView) loginView.findViewById(R.id.status);
        signInButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (loginBtnClickedListener != null) {
                    loginBtnClickedListener.onLoginBtnClicked();
                }
            }
        });
        signInButton.setOnClickListener(this);
        // Large sign-in
        ((SignInButton) loginView.findViewById(R.id.sign_in_button)).setSize(SignInButton.SIZE_WIDE);
        statusTextView = (TextView) loginView.findViewById(R.id.status);

        // Adding rest of the listeners
        loginView.findViewById(R.id.sign_out_button).setOnClickListener(this);
        loginView.findViewById(R.id.sign_out_and_disconnect).setOnClickListener(this);

        return loginView;
    }


    // TODO: Rename method, update argument and hook method into UI event
    public void onLoginButtonClicked(Uri uri) {
        if (listener != null) {
            listener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (OnFragmentInteractionListener) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public void onStart() {
        super.onStart();
        googleApiClient.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Login Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.umdcs4995.whiteboard/http/host/path")
        );
        AppIndex.AppIndexApi.start(googleApiClient, viewAction);

        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(googleApiClient);
        if (opr.isDone()) {
            // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
            // and the GoogleSignInResult will be available instantly.
            Log.d(TAG, "Got cached sign-in");
            GoogleSignInResult result = opr.get();
            handleSignInResult(result);
        } else {
            // If the user has not previously signed in on this device or the sign-in has expired,
            // this asynchronous branch will attempt to sign in the user silently.  Cross-device
            // single sign-on will occur in this branch.
            showProgressDialog();
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(GoogleSignInResult googleSignInResult) {
                    hideProgressDialog();
                    handleSignInResult(googleSignInResult);
                }
            });
        }
//        if (Plus.PeopleApi.getCurrentPerson(googleApiClient) != null) {
//            Person currentPerson = Plus.PeopleApi.getCurrentPerson(googleApiClient);
//            String personName = currentPerson.getDisplayName();
//            String personPhoto = currentPerson.getImage().getUrl();
//            String personGooglePlusProfile = currentPerson.getUrl();
//        }
    }

    public void onStop() {
        super.onStop();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Login Page", // TODO: Define a title for the content shown.
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
            statusTextView.setText(getString(R.string.signed_in_fmt, acct.getDisplayName()));
            //personPhoto = acct.getPhotoUrl();

            updateUI(true);

        } else {
            //Signed Out, show unathenticated UI.
            updateUI(false);
        }
    }

    private void updateUI(boolean signedIn) {
        if (googleApiClient.isConnected()) {

            loginView.findViewById(R.id.sign_in_button).setVisibility(View.GONE);
            loginView.findViewById(R.id.sign_out_and_disconnect).setVisibility(View.VISIBLE);
            //this.finish();
            googleApiClient.connect();
            if (googleApiClient.isConnected()) {
                Person currentPerson = Plus.PeopleApi.getCurrentPerson(googleApiClient);
                 if (currentPerson != null) {
//                   //Show signed-in user's name
//                   String name = currentPerson.getDisplayName();
//                   statusTextView.setText(getString(R.string.signed_in_fmt, name));

                     // Show users' email address (which requires GET_ACCOUNTS permission)
                     if (checkAccountsPermission()) {
//                      String currentAccount = Plus.AccountApi.getAccountName(googleApiClient);
//                      ((TextView) loginView.findViewById(R.id.detail)).setText(currentAccount);
                     }
                 }
            } else {
                // If getCurrentPerson returns null there is most likely an error with the
                // configuration of the application (Invalid Client ID, Api's not enabled, etc.)
                statusTextView.setText(R.string.signed_out);
                loginView.findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
                loginView.findViewById(R.id.sign_out_and_disconnect).setVisibility(View.GONE);
            }
            // Set button visibility
            loginView.findViewById(R.id.sign_in_button).setVisibility(View.GONE);
            loginView.findViewById(R.id.sign_out_button).setVisibility(View.VISIBLE);
            loginView.findViewById(R.id.sign_out_and_disconnect).setVisibility(View.VISIBLE);
        } else {
            // Show signed-out message
            statusTextView.setText(R.string.signed_out);

            // Set button visibility
            loginView.findViewById(R.id.sign_in_button).setEnabled(true);
            loginView.findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
            loginView.findViewById(R.id.sign_out_and_disconnect).setVisibility(View.GONE);
        }
    }

    private void onSignInClicked() {
        // User clicked the sign-in button, so begin the sign-in process and automatically
        // attempt to resolve any errors that occur.
        mShouldResolve = true;
        googleApiClient.connect();

        // Show a message to the user that we are signing in.
        statusTextView.setText(R.string.signing_in);
        Log.d(TAG, "In sign in clicked");
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void onSignOutClicked() {
        // Clear the default account so that GoogleApiClient will not automatically
        // connect in the future.
        if (googleApiClient.isConnected()) {
            Plus.AccountApi.clearDefaultAccount(googleApiClient);

            Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(
                    new ResultCallback<Status>() {
                        @Override
                        public void onResult(Status status) {
                            // [START_EXCLUDE]
                            updateUI(false);
                            // [END_EXCLUDE]
                        }
                    });
        } if (googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
        updateUI(false);
    }

    private void onDisconnectClicked() {
        // Revoke all granted permissions and clear the default account.  The user will have
        // to pass the consent screen to sign in again.
        if (googleApiClient.isConnected()) {
            Plus.AccountApi.clearDefaultAccount(googleApiClient);
            Plus.AccountApi.revokeAccessAndDisconnect(googleApiClient);
            googleApiClient.disconnect();
        }
        updateUI(false);
    }

        /**
         * Check if we have the GET_ACCOUNTS permission and request it if we do not.
         * @return true if we have the permission, false if we do not.
         */
    private boolean checkAccountsPermission() {
        final String perm = Manifest.permission.GET_ACCOUNTS;
        Context context = getContext();
        final Activity activity = getActivity();
        int permissionCheck = ContextCompat.checkSelfPermission(context, perm);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            // We have the permission
            return true;
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(activity, perm)) {
            // Need to show permission rationale, display a snackbar and then request
            // the permission again when the snackbar is dismissed.
            Snackbar.make(loginView.findViewById(R.id.login),
                    R.string.contacts_permission_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // Request the permission again.
                            ActivityCompat.requestPermissions(activity,
                                    new String[]{perm},
                                    RC_PERM_GET_ACCOUNTS);
                        }
                    }).show();
            return false;
        } else {
            // No explanation needed, we can request the permission.
            ActivityCompat.requestPermissions(activity,
                    new String[]{perm},
                    RC_PERM_GET_ACCOUNTS);
            return false;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_IS_RESOLVING, mIsResolving);
        outState.putBoolean(KEY_SHOULD_RESOLVE, mShouldResolve);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                onSignInClicked();
                break;
            case R.id.sign_out_button:
                onSignOutClicked();
                break;
        }
    }

    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getContext());
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

    public void onConnectionFailed(ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.w(TAG, "onConnectionFailed: " + connectionResult);
        if (!mIsResolving && mShouldResolve) {
            if (connectionResult.hasResolution()) {
                try {
                    Activity activity = getActivity();
                    connectionResult.startResolutionForResult(activity, RC_SIGN_IN);
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
            updateUI(false);
        }
    }

    private void showErrorDialog(ConnectionResult connectionResult) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Context context = getContext();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(context);

        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                Activity activity = getActivity();
                apiAvailability.getErrorDialog(activity, resultCode, RC_SIGN_IN,
                        new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                mShouldResolve = false;
                                updateUI(false);
                            }
                        }).show();
            } else {
                Log.w(TAG, "Google Play Services Error:" + connectionResult);
                String errorString = apiAvailability.getErrorString(resultCode);
                //Toast.makeText(this, errorString, Toast.LENGTH_SHORT).show();

                mShouldResolve = false;
                updateUI(false);
            }
        }
    }

    private void revokeAccess() {
        Auth.GoogleSignInApi.revokeAccess(googleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        // [START_EXCLUDE]
                        updateUI(false);
                        // [END_EXCLUDE]
                    }
                });
    }

    public static class GoogleSignInActivityResult {
        public int mRequestCode;
        public int mResultCode;
        public Intent mData;

        public GoogleSignInActivityResult(int requestCode, int resultCode, Intent data) {
            mRequestCode = requestCode;
            mResultCode = resultCode;
            mData = data;
        }
    }

}
