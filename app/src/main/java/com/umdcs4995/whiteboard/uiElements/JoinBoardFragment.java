package com.umdcs4995.whiteboard.uiElements;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
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
 * Activity for handling the list of whiteboards that the user can join
 */

public class JoinBoardFragment extends Fragment {

    ContactList whiteboardList = new ContactList();
    DialogFragment newBoardFragment = new NewBoardFragment();

    /**
     * Called on creation of the fragment.
     * @param savedInstanceState
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_whiteboard_list, container, false);
        Button button = (Button) view.findViewById(R.id.create_button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                newBoardFragment = NewBoardFragment.newInstance();
                newBoardFragment.show(getActivity().getFragmentManager(), "AddBoardDialog");
            }
        });
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
        whiteboardList = new ContactList();
        fetchWhiteboardlist();
    }

    /**
     * Creates the contact list view and adapters.
     */
    private void setupWhiteboardListView() {
        //First get some strings
        ContactWb[] people = new ContactWb[whiteboardList.getSize()];

        //Pull the contact list in.
        for(int i = 0; i < whiteboardList.getSize(); i++) {
            people[i] = whiteboardList.getContactOrdinal(i);
        }

        ListAdapter customAdapter = new ContactListAdapter(this.getContext(), people);
        //Grab the list view and set the adapter.

        ListView listView = (ListView) getView().findViewById(R.id.contact_listview);
        listView.setAdapter(customAdapter);
        listView.setOnItemClickListener(makeWhiteboardListListener());
    }

    /**
     * Make an item lick listener for the whiteboards to join.
     * Sends a JSON request via socketIO and logs the response
     */
    private AdapterView.OnItemClickListener makeWhiteboardListListener() {
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
     * Fetches the JSON file at
     * http[s]://[servername]:[port]/whiteboards.json
     * and parses it, adding each entry to the 'ContactList' object (that's really a whiteboard list)
     */
    private void fetchWhiteboardlist() {
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
                                whiteboardList.addContact(whiteboard, whiteboard, ContactList.STATUS_CONNECTED);
                            } catch (JSONException e) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getContext(), "Error parsing whiteboards.json from server", Toast.LENGTH_LONG);
                                    }
                                });
                            }
                        }
                        setupWhiteboardListView();
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
