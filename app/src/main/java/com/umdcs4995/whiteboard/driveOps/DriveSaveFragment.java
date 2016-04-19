package com.umdcs4995.whiteboard.driveOps;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.IntentSender.SendIntentException;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi.DriveContentsResult;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.umdcs4995.whiteboard.MainActivity;
import com.umdcs4995.whiteboard.R;
import com.umdcs4995.whiteboard.uiElements.WhiteboardDrawFragment;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link DriveSaveFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DriveSaveFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = "DriveSaveFragment";

    /**
     * Integer representation of code that is used when sending an intent to start an OpenFileActivity for a result. -LJK
     */
    private static final int DRIVE_OPEN_CODE = 0;
    private static final int REQUEST_CODE_CREATOR = 2;
    private static final int REQUEST_CODE_RESOLUTION = 1; //3; // change
    private static final int REQUEST_ACCOUNT_PICKER = 2; // new
    private static final int REQUEST_AUTHORIZATION = 1; // new
    private static final int RESULT_STORE_FILE = 4;
    private static final int RC_SIGN_IN = 9001;
    private final HttpTransport m_transport = AndroidHttp.newCompatibleTransport(); // new
    private final JsonFactory m_jsonFactory = GsonFactory.getDefaultInstance(); // new


    private GoogleAccountCredential credential;

    /* Client for accessing Google APIs */
    private GoogleApiClient googleApiClient;
    private com.google.api.services.drive.Drive client;
    private Bitmap bitmapToSave;

    private View driveSaveView, drawView;
    private ProgressBar progressBar;
    private static Uri fileURI;
    private static com.google.api.services.drive.Drive service;
    private List<File> resultList;
    private ListView listView;
    private String[] fileArray;
    private ArrayAdapter adapter;


    private OnDriveSaveButtonClickedListener onDriveSaveButtonClickedListener;

    /* Keys for persisting instance variables in savedInstanceState */
    private static final String KEY_IS_RESOLVING = "is_resolving";
    private static final String KEY_SHOULD_RESOLVE = "should_resolve";

    /* Is there a ConnectionResult resolution in progress? */
    private boolean mIsResolving = false;

    /* Should we automatically resolve ConnectionResults when possible? */
    private boolean mShouldResolve = false;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    Fragment whiteboardDrawFragment = new WhiteboardDrawFragment();

    public DriveSaveFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DriveSaveFragment.
     */
    public static DriveSaveFragment newInstance(String param1, String param2) {
        DriveSaveFragment fragment = new DriveSaveFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public interface OnDriveSaveButtonClickedListener {
        public void onDriveSaveButtonClicked();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "made it to driveSaveFrag");
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        credential = GoogleAccountCredential.usingOAuth2(getActivity().getApplicationContext(), Arrays.asList(DriveScopes.DRIVE));

        com.google.api.services.drive.Drive service = getDriveService(credential);

        SharedPreferences settings = getActivity().getPreferences(Context.MODE_PRIVATE);
        credential = GoogleAccountCredential.usingOAuth2(getActivity().getApplicationContext(), Arrays.asList(DriveScopes.DRIVE));

        googleApiClient = ((MainActivity)getActivity()).getGoogleApiClient();

        googleApiClient.connect();


        if (googleApiClient.isConnected() == false) {
            googleApiClient.connect();
        }
        startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
//        //listView = (ListView) driveSaveView.findViewById(R.id.driveSaveListView);
//        final Button driveSaveButton = (Button) driveSaveView.findViewById(R.id.driveSaveBtn);
//        driveSaveButton.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                final Intent storeIntent = new Intent(Intent.ACTION_PICK);
//                storeIntent.setType("*/*");
//                startActivityForResult(storeIntent, RESULT_STORE_FILE);
//            }
//        });
//        client = new com.google.api.services.drive.Drive.Builder(
//                m_transport, m_jsonFactory, credential).setApplicationName("Whiteboard/1.0")
//                .build();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        super.onCreateView(inflater, container, savedInstanceState);
        driveSaveView = inflater.inflate(R.layout.fragment_drive_save, container, false);

        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this.getContext())
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .addScope(Drive.SCOPE_APPFOLDER) // required for App Folder sample
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
        googleApiClient.connect();
        listView = (ListView) driveSaveView.findViewById(R.id.driveSaveListView);
        final Button driveSaveButton = (Button) driveSaveView.findViewById(R.id.driveSaveBtn);
        return driveSaveView;
    }

//    private void populateListView()
//    {
//        runOnUiThread(new Runnable()
//        {
//            @Override
//            public void run()
//            {
//                mFileArray = new String[mResultList.size()];
//                int i = 0;
//                for(File tmp : mResultList)
//                {
//                    mFileArray[i] = tmp.getTitle();
//                    i++;
//                }
//                mAdapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_list_item_1, mFileArray);
//                mListView.setAdapter(mAdapter);
//            }
//        });
//    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
//        mListener = (OnFragmentInteractionListener) context;
        this.whiteboardDrawFragment = (Fragment) whiteboardDrawFragment;
//
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * Callback for GoogleApiClient connection success
     */
    @Override
    public void onConnected(Bundle bundle) {
        // onConnected indicates that an account was selected on the device, that the selected account
        // has granted any requested permissions to our app and that we were able to establish a service
        // connection to Google Play services
        Log.d(TAG, "API client connected" + bundle);
        mShouldResolve = false;
    }

    /**
     * Callback for suspension of current connection
     */
    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost. The GoogleApiClient will automatically
        // attempt to re-connect. Any UI elements that depend on connection to Google APIs should be
        // hidden or disabled until onConnected is called again
        Log.w(TAG, "GoogleApiClient connection suspended: " + cause);
    }

    @Override
    public void onStart() {
        super.onStart();
        googleApiClient.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Save To Drive Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.umdcs4995.whiteboard/http/host/path")
        );

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // handle the result from the startActivityForResult
        switch (requestCode) {
            case REQUEST_ACCOUNT_PICKER:
                Log.d(TAG, "in onActivityResult");
                if (resultCode == Activity.RESULT_OK && data != null && data.getExtras() != null) {
                    String accountName = data.getExtras().getString(AccountManager.KEY_ACCOUNT_NAME);
                    Log.d(TAG, "in onActivityResult; data != null");

                    if (accountName != null) {
                        Log.d(TAG, "in onActivityResult: got account name");

                        credential.setSelectedAccountName(accountName);
                        service = getDriveService(credential);
                    }
                }
                try {
                    Log.d(TAG, "in on activity result about to call save to Drive");
                    saveToDrive();

                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == Activity.RESULT_OK) {
                    // account is already picked
                } else {
                    startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
                }
                break;
            case RESULT_STORE_FILE:
                fileURI = data.getData();
                //save the file to google drive
                try {
                    Log.d(TAG, "in on activity result about to call save to Drive");
                    saveToDrive();

                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    private com.google.api.services.drive.Drive getDriveService(GoogleAccountCredential credential) {
        return new com.google.api.services.drive.Drive.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(), credential)
                .setApplicationName("Whiteboard").build();
    }


    public void saveToDrive() throws IOException {
        Log.d(TAG, "made it to saveToDrive()");
        googleApiClient.connect();
//        Thread t = new Thread(new Runnable() {
//
//            @Override
//            public void run() {
//                Log.i(TAG, "Inside the thread Creating new contents.");


        Bundle bundle = this.getArguments();
        final Bitmap image = BitmapFactory.decodeByteArray(bundle.getByteArray("byteArray"), 0, bundle.getByteArray("byteArray").length);

        Drive.DriveApi.newDriveContents(googleApiClient)
                .setResultCallback(new ResultCallback<DriveContentsResult>() {
                    @Override
                    public void onResult(DriveContentsResult result) {
                        // if the operation was not successful, we cannot do anything
                        // and must fail
                        if (!result.getStatus().isSuccess()) {
                            Log.i(TAG, "Failed to create new contents.");
                            return;
                        }
                        // Otherwise we can write our data to the new contents.
                        Log.i(TAG, "New contents created.");
                        // Get an output stream for the result
                        OutputStream outputStream = result.getDriveContents().getOutputStream();
                        // Write the bitmap data from it
                        ByteArrayOutputStream bitmapStream = new ByteArrayOutputStream();
                        image.compress(CompressFormat.PNG, 100, bitmapStream);
                        try {
                            outputStream.write(bitmapStream.toByteArray());
                            Log.i(TAG, "wrote file contents");
                        } catch (IOException e) {
                            Log.i(TAG, "Unable to write file contents");
                        }
                        // Create the initial metadata - MIME type and title
                        // Note that the user will be able to change the title later, this is
                        // just a default
                        MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
                                .setMimeType("image/jpeg").setTitle("Android Photo.png").build();
                        // Create an intent for the file chooser, and start it.
                        IntentSender intentSender = Drive.DriveApi
                                .newCreateFileActivityBuilder()
                                .setInitialMetadata(metadataChangeSet)
                                .setInitialDriveContents(result.getDriveContents())
                                .build(googleApiClient);
                        Log.d(TAG, "built the intentSender");
                        try {
                            getActivity().startIntentSenderForResult(
                                    intentSender, REQUEST_CODE_CREATOR, null, 0, 0, 0);
                            Log.i(TAG, "Launched file chooser");
                        } catch (SendIntentException e) {
                            Log.i(TAG, "Failed to launch file chooser.");
                        }
                    }
                });
        Log.d(TAG, "about to call fragment manager to remove this fragment");
        getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {

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


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
