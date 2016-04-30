package com.umdcs4995.whiteboard.uiElements;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
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
import com.umdcs4995.whiteboard.services.ConnectivityException;
import com.umdcs4995.whiteboard.services.SocketService;
import com.umdcs4995.whiteboard.whiteboarddata.Buddy;
import com.umdcs4995.whiteboard.whiteboarddata.GoogleUser;

import org.json.JSONObject;

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

        //clear the temporary list
        buddies.clear();

        //add the current user to the top of the buddy list.
        buddies.add(Globals.getInstance().getClientUser());

        //Test method.
        setupTestContacts();

        //Listener for the buddy list receiver
        final SocketService ss = Globals.getInstance().getSocketService();
        ss.addListener(SocketService.Messages.LISTBUDDIES, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.i("SOCKETSERVICE", "Incoming buddy list");
                JSONObject jo = (JSONObject) args[0];
                Log.v(TAG, "INCOMING: " + jo.toString());

                try {
                    //BuddyListProtocol.execute(ja);
                } catch (NullPointerException e) {
                    Log.e(TAG, "NullpointerError Error, malformed string");
                }
                ss.clearListener(SocketService.Messages.LISTBUDDIES);
            }
        });

        setupContactListView();
    }

    /**
     * Briefly setup some test contacts.
     */
    private void setupTestContacts() {

        buddies.add(new Buddy("Robert DeGree", "rob@whiteboard.com", " ", false));
        buddies.add(new Buddy("Robert DeGree", "rob@whiteboard.com", " ", false));
        buddies.add(new Buddy("Robert DeGree", "rob@whiteboard.com", " ", false));
        buddies.add(new Buddy("Robert DeGree", "rob@whiteboard.com", " ", false));
        buddies.add(new Buddy("Robert DeGree", "rob@whiteboard.com", " ", false));
        buddies.add(new Buddy("Robert DeGree", "rob@whiteboard.com", " ", false));
        try {
            Globals.getInstance().getWhiteboardProtocol().outBuddyRequest();
        } catch (ConnectivityException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates the contact list view and adapters.
     */
    private void setupContactListView() {
        //First get some strings
        GoogleUser[] people = new GoogleUser[buddies.size()];

        //Pull the contact list in.
        for(int i = 0; i < buddies.size(); i++) {
            people[i] = buddies.get(i);
        }

        ListAdapter customAdapter = new BuddyListAdapter(this.getContext(), people);
        //Grab the list view and set the adapter.


        ListView listView = (ListView) getView().findViewById(R.id.contact_listview);
        listView.setAdapter(customAdapter);
        listView.setOnItemClickListener(makeContactListListener());
    }

    /**
     * Make an item lick listener for the contacts.
     * @return
     */
    private AdapterView.OnItemClickListener makeContactListListener() {
        AdapterView.OnItemClickListener l = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //This is where the code goes for a click of an item of the list.
                ContactWb person = (ContactWb) parent.getItemAtPosition(position);
                new NotYetImplementedToast(getContext(), person.getName() + " clicked!");
            }
        };

        return l;
    }

    private class LoadProfileImage extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public LoadProfileImage(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }

}
