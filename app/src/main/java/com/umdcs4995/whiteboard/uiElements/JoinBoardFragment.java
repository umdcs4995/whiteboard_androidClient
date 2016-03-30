package com.umdcs4995.whiteboard.uiElements;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.umdcs4995.whiteboard.Globals;
import com.umdcs4995.whiteboard.R;
import com.umdcs4995.whiteboard.services.SocketService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.Socket;
import java.util.logging.Logger;

import contacts.ContactList;
import contacts.ContactListAdapter;
import contacts.ContactWb;
import io.socket.emitter.Emitter;

/**
 * Activity for handling the contact list screen for the app.
 */

public class JoinBoardFragment extends Fragment {

    ContactList contactList = new ContactList();

    /**
     * Called on creation of the fragment.
     * @param savedInstanceState
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_whiteboard_list, container, false);
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
        fetchStreamlist();
    }

    /**
     * Creates the contact list view and adapters.
     */
    private void setupContactListView() {
        //First get some strings
        ContactWb[] people = new ContactWb[contactList.getSize()];

        //Pull the contact list in.
        for(int i = 0; i < contactList.getSize(); i++) {
            people[i] = contactList.getContactOrdinal(i);
        }

        ListAdapter customAdapter = new ContactListAdapter(this.getContext(), people);
        //Grab the list view and set the adapter.

        ListView listView = (ListView) getView().findViewById(R.id.contact_listview);
        listView.setAdapter(customAdapter);
        listView.setOnItemClickListener(makeContactListListener());
    }

    /**
     * Make an item lick listener for the contacts.
     * @return the item click listener associated with the contact list
     */
    private AdapterView.OnItemClickListener makeContactListListener() {
        AdapterView.OnItemClickListener l = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //This is where the code goes for a click of an item of the list.
                ContactWb person = (ContactWb) parent.getItemAtPosition(position);
                JSONObject joinWbRequest = new JSONObject();
                try {
                    joinWbRequest.put("name", person.getName());
                } catch (JSONException e) {
                    Log.i("joinWhiteboard", "failed to join whiteboard");
                }

                final SocketService socket = Globals.getInstance().getSocketService();

                socket.addListener(SocketService.Messages.JOIN_WHITEBOARD, new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        JSONObject recvd = (JSONObject)args[0];
                        try {
                            Log.i("joinWhiteboard", "received message: " + recvd.getString("message"));
                        } catch (JSONException e) {
                            Log.w("joinWhiteboard", "error parsing received message");
                        }
                        socket.clearListener(SocketService.Messages.JOIN_WHITEBOARD);
                    }
                });

                socket.sendMessage(SocketService.Messages.JOIN_WHITEBOARD, joinWbRequest);

            }
        };

        return l;
    }

    /**
     * fetches the stream list for the json requests
     */
    private void fetchStreamlist() {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(getActivity());
        String url = Globals.getInstance().getServerAddress() + "/whiteboards.json";
        Logger.getAnonymousLogger().info(url);


        // Request a string response from the provided URL.
        JsonArrayRequest jsonRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        for(int i = 0; i < response.length(); i++) {
                            try {
                                String whiteboard = response.getString(i);
                                contactList.addContact(whiteboard, whiteboard, ContactList.STATUS_CONNECTED);
                            } catch (JSONException e) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getContext(), "Error parsing whiteboards.json from server", Toast.LENGTH_LONG);
                                    }
                                });
                            }
                        }
                        setupContactListView();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(), "Error retrieving whiteboards.json from server", Toast.LENGTH_LONG);
                    }
                });
            }
        });

        // Add the request to the RequestQueue.
        queue.add(jsonRequest);
        queue.start();
    }

}
