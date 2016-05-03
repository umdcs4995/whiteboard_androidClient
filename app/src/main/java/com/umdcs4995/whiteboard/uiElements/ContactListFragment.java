package com.umdcs4995.whiteboard.uiElements;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.umdcs4995.whiteboard.Globals;
import com.umdcs4995.whiteboard.MainActivity;
import com.umdcs4995.whiteboard.R;
import com.umdcs4995.whiteboard.protocol.BuddyListProtocol;
import com.umdcs4995.whiteboard.services.ConnectivityException;
import com.umdcs4995.whiteboard.services.SocketService;
import com.umdcs4995.whiteboard.whiteboarddata.GoogleUser;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;

import contacts.ContactList;
import contacts.ContactListAdapter;
import contacts.ContactWb;
import io.socket.emitter.Emitter;

/**
 * Activity for handling the contact list screen for the app.
 */

public class ContactListFragment extends Fragment {

    LinkedList<GoogleUser> buddies = new LinkedList<>();
    private final String TAG = "ContactListFragment";

    /**
     * Called on creation of the fragment.
     * @param savedInstanceState
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.fragment_contact_list, container, false);
        return view;
    }

    /**
     * Called after onCreateView.  Important because setupContactListView() requires that the
     * view has been created and set.
     * @param view
     * @param savedInstanceState
     */
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        //Listener for the buddy list receiver
        final SocketService ss = Globals.getInstance().getSocketService();
        try {
            Globals.getInstance().getWhiteboardProtocol().outBuddyRequest();
        } catch (ConnectivityException e) {
            e.printStackTrace();
        }
        ss.addListener(SocketService.Messages.LISTBUDDIES, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.i("SOCKETSERVICE", "Incoming buddy list");
                JSONObject jo = (JSONObject) args[0];
                Log.v(TAG, "INCOMING: " + jo.toString());

                try {
                    BuddyListProtocol.execute(jo);
                    buddies = Globals.getInstance().getWhiteboard().getBuddies();
                    GoogleUser[] gus = new GoogleUser[buddies.size()];
                    for(int i = 0; i < buddies.size(); i++) {
                        gus[i] = buddies.get(i);
                    }
                    new LoadProfileImages().execute(gus);
                } catch (NullPointerException e) {
                    Log.e(TAG, "NullpointerError Error, malformed string");
                }
                ss.clearListener(SocketService.Messages.LISTBUDDIES);
                setupContactListView();

            }
        });


    }

    /**
     * Briefly setup some test contacts.
     */
    private void setupTestContacts() {


    }

    /**
     * Creates the contact list view and adapters.
     */
    private void setupContactListView() {
        buddies = Globals.getInstance().getWhiteboard().getBuddies();
        //First get some strings
        GoogleUser[] people = new GoogleUser[buddies.size()];

        //Pull the contact list in.
        for(int i = 0; i < buddies.size(); i++) {
            people[i] = buddies.get(i);
        }

        final ListAdapter customAdapter = new BuddyListAdapter(this.getContext(), people);
        //Grab the list view and set the adapter.

        MainActivity ma = (MainActivity) getActivity();
        ma.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ListView listView = (ListView) getView().findViewById(R.id.contact_listview);
                listView.setAdapter(customAdapter);
            }
        });

    }


    /**
     * This background task loads the profile images in the background and then displays them
     * when finished
     */
    private class LoadProfileImages extends AsyncTask<GoogleUser, Void, Void> {

        protected Void doInBackground(GoogleUser... gus) {
            Log.v("LOADPROFILEIMAGETASK", "In Background");
            for(int i = 0; i < gus.length; i++) {
                Log.v("LOADPROFILEIMAGETASK", "Loading image: " + i);
                GoogleUser gu = gus[i];

                InputStream in = null;

                try {
                    in = new java.net.URL(gu.getProfileURL()).openStream();
                    Bitmap downloadedPic = BitmapFactory.decodeStream(in);
                    gu.setImage(downloadedPic);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            setupContactListView();
            super.onPostExecute(aVoid);
        }
    }

}
