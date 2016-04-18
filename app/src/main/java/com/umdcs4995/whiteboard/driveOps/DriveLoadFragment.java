package com.umdcs4995.whiteboard.driveOps;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
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
import android.widget.ProgressBar;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi.DriveContentsResult;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFile.DownloadProgressListener;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.OpenFileActivityBuilder;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.DriveScopes;
import com.umdcs4995.whiteboard.MainActivity;
import com.umdcs4995.whiteboard.R;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Arrays;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DriveLoadFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DriveLoadFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DriveLoadFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = "DriveLoadFragment";
    private static final int REQUEST_ACCOUNT_PICKER = 2; // new


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private static com.google.api.services.drive.Drive service;


    /**
     * Request code to handle the result from file opening activity.
     */
    private static final int REQUEST_CODE_OPENER = 1;

    protected static final int REQUEST_CODE_RESOLUTION = 1;

    /**
     * Progress bar to show the current download progress of the file.
     */
    private ProgressBar mProgressBar;

    /**
     * File that is selected with the open file activity.
     */
    private DriveId mSelectedFileDriveId;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    private GoogleAccountCredential googleAccountCredential;

    private View driveLoadView;



    public DriveLoadFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DriveLoadFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DriveLoadFragment newInstance(String param1, String param2) {
        DriveLoadFragment fragment = new DriveLoadFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Made it to drive load fragment");
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        googleAccountCredential = GoogleAccountCredential.usingOAuth2(getActivity().getApplicationContext(), Arrays.asList(DriveScopes.DRIVE));
        com.google.api.services.drive.Drive service = getDriveService(googleAccountCredential);

        SharedPreferences settings = getActivity().getPreferences(Context.MODE_PRIVATE);

        client = ((MainActivity)getActivity()).getGoogleApiClient();
        client.connect();

        if (client.isConnected() == false) {
            client.connect();
        }
        startActivityForResult(googleAccountCredential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        super.onCreateView(inflater, container, savedInstanceState);
        driveLoadView = inflater.inflate(R.layout.fragment_drive_load, container, false);
        if (client == null) {
            Log.d(TAG, "Creating client");
            client = new GoogleApiClient.Builder(this.getContext())
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .addScope(Drive.SCOPE_APPFOLDER)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
        client.connect();
        return driveLoadView;
    }


    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
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
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {

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

    private com.google.api.services.drive.Drive getDriveService(GoogleAccountCredential credential) {
        return new com.google.api.services.drive.Drive.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(), credential)
                .setApplicationName("Whiteboard").build();
    }


    @Override
    public void onConnected(Bundle bundle) {
        // onConnected indicates that an account was selected on the device, that the selected account
        // has granted any requested permissions to our app and that we were able to establish a service
        // connection to Google Play services
        Log.d(TAG, "API client connected" + bundle);
        if (mSelectedFileDriveId != null) {
            open();
            Log.i(TAG, "In onConnected about to finish");
            //finish();
        }

        // Let the user pick an mp4 or a jpeg file if there are
        // no files selected by the user.
        Log.d(TAG, "made it to on connected");
        IntentSender intentSender = Drive.DriveApi
                .newOpenFileActivityBuilder()
                .setMimeType(new String[]{"video/mp4", "image/jpeg", "image/png", "image/gif", "application/vnd.google-apps.document",
                        "application/vnd.google-apps.drawing", "application"})
                .build(client);
        try {
            getActivity().startIntentSenderForResult(intentSender, REQUEST_CODE_OPENER, null, 0, 0, 0);
        } catch (SendIntentException e) {
            Log.w(TAG, "Unable to send intent", e);
        }

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
        driveFile.open(client, DriveFile.MODE_READ_ONLY, new DownloadProgressListener() {
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
            Intent resultIntent = new Intent(getContext(), MainActivity.class);
            resultIntent.putExtra("image", bytes);
            getActivity().setResult(Activity.RESULT_OK, resultIntent);
            Log.d(TAG, "made it to just before finish");
            startActivity(resultIntent);


            //finish();
            //Log.d(TAG, "somehow after finish");
        }

        ;
    };
    //finish();
    /**
     * Called when {@code mGoogleApiClient} is disconnected.
     */
    @Override
    public void onConnectionSuspended(int cause) {
        Log.i(TAG, "GoogleApiClient connection suspended");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "In onactivityResult");
///
//        Log.d(TAG, "API client connected");
//        if (mSelectedFileDriveId != null) {
//            open();
//            Log.i(TAG, "In onActivityResult about to finish");
//            //finish();
//        }

///
        switch (requestCode) {
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == Activity.RESULT_OK && data != null && data.getExtras() != null) {
                    String accountName = data.getExtras().getString(AccountManager.KEY_ACCOUNT_NAME);
                    Log.d(TAG, "in onActivityResult; data != null");

                    if (accountName != null) {
                        Log.d(TAG, "in onActivityResult: got account name");

                        googleAccountCredential.setSelectedAccountName(accountName);
                        service = getDriveService(googleAccountCredential);
                    }
                    // Let the user pick an mp4 or a jpeg file if there are
                    // no files selected by the user.
                    Log.d(TAG, "About to send intentsender");
                    IntentSender intentSender = Drive.DriveApi
                            .newOpenFileActivityBuilder()
                            .setMimeType(new String[]{"video/mp4", "image/jpeg", "image/png", "image/gif", "application/vnd.google-apps.document",
                                    "application/vnd.google-apps.drawing", "application"})
                            .build(client);
                    try {
                        getActivity().startIntentSenderForResult(intentSender, REQUEST_CODE_OPENER, null, 0, 0, 0);
                    } catch (SendIntentException e) {
                        Log.w(TAG, "Unable to send intent", e);
                    } catch (Exception e) {
                        Log.d(TAG, "Some exception with sending intentsender");
                    }
                }
                break;
            case REQUEST_CODE_OPENER:
                if (resultCode == Activity.RESULT_OK) {
                    Log.d(TAG, "in onactivityResult inside request code opener ok");
                    mSelectedFileDriveId = (DriveId) data.getParcelableExtra(
                            OpenFileActivityBuilder.EXTRA_RESPONSE_DRIVE_ID);

                    open();
                } else {
                    Log.d(TAG, "result not ok ");
                }
                break;
        }
//        if (/*requestCode == REQUEST_CODE_OPENER &&*/ resultCode == Activity.RESULT_OK) {
//            Log.d(TAG, "in onactivityResult result ok");
//            mSelectedFileDriveId = (DriveId) data.getParcelableExtra(
//                    OpenFileActivityBuilder.EXTRA_RESPONSE_DRIVE_ID);
//
//            open();
//        } else {
//            Log.d(TAG, "in onActivityResult, result failed");
//            super.onActivityResult(requestCode, resultCode, data);
//        }
    }
    /**
     * Called when {@code mGoogleApiClient} is trying to connect but failed.
     * Handle {@code result.getResolution()} if there is a resolution is
     * available.
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "GoogleApiClient connection failed: " + result.toString());
        if (!result.hasResolution()) {
            // show the localized error dialog.
            GoogleApiAvailability.getInstance().getErrorDialog(getActivity(), result.getErrorCode(), 0).show();
            return;
        }
        try {
            result.startResolutionForResult(getActivity(), REQUEST_CODE_RESOLUTION);
        } catch (SendIntentException e) {
            Log.e(TAG, "Exception while starting resolution activity", e);
        }
    }

    /**
     * Shows a toast message.
     */
//    public void showMessage(String message) {
//        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
//    }

}
