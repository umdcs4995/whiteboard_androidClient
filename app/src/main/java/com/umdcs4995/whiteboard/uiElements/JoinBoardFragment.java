package com.umdcs4995.whiteboard.uiElements;

import android.annotation.TargetApi;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.app.DialogFragment;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.umdcs4995.whiteboard.MainActivity;
import com.umdcs4995.whiteboard.R;
import com.umdcs4995.whiteboard.services.SocketService;
import com.umdcs4995.whiteboard.whiteboarddata.Whiteboard;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.logging.Logger;

import contacts.ContactList;
import contacts.ContactListAdapter;
import contacts.ContactWb;
import io.socket.emitter.Emitter;
import io.socket.global.Global;

/**
 * Activity for handling the list of whiteboards that the user can join
 */

public class JoinBoardFragment extends Fragment {

    ContactList whiteboardList = new ContactList();
    private SocketService socketService = Globals.getInstance().getSocketService();
    private int mStackLevel = 0;

    /**
     * Called on creation of the fragment.
     * @param savedInstanceState A previous instance to revert to.
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_whiteboard_list, container, false);

        // Setup the Create New Whiteboard button
        Button button = (Button) view.findViewById(R.id.create_button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                // Get the layout inflater
                LayoutInflater inflater = getActivity().getLayoutInflater();
                final View dialogView = inflater.inflate(R.layout.fragment_new_whiteboard, null);
                builder.setView(dialogView);

                final EditText whiteboardName = (EditText) dialogView.findViewById(R.id.txt_board_name);

                builder.setTitle(R.string.dialog_new_whiteboard_title);
                builder.setPositiveButton(R.string.create, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        JSONObject addWbRequest = new JSONObject();
                        try {
                            addWbRequest.put("name", whiteboardName.getText());
                        } catch (JSONException e) {
                            Toast.makeText(Globals.getInstance().getGlobalContext(), "Error creating whiteboard", Toast.LENGTH_LONG).show();
                        }

                        try {
                            socketService.sendMessage(SocketService.Messages.CREATE_WHITEBOARD, addWbRequest);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        socketService.addListener(SocketService.Messages.CREATE_WHITEBOARD, new Emitter.Listener() {
                            @Override
                            public void call(Object... args) {
                                JSONObject recvd = (JSONObject) args[0];
                                try {
                                    Log.i("createWhiteboard", "received message: " + recvd.getString("message"));
                                    final MainActivity mainActivity = (MainActivity) getActivity();
                                    mainActivity.runOnUiThread(new Runnable() {

                                        @Override
                                        public void run() {
                                            try {
                                                mainActivity.setTitleBarText(whiteboardName.getText().toString());
                                            } catch (Exception e) {

                                            }
                                        }

                                    });
                                    //Fragment suicides here
                                    Globals.getInstance().setWhiteboard(new Whiteboard(whiteboardName.getText().toString()));
                                    mainActivity.onFragmentSuicide(SuicidalFragment.POP_ME);
                                } catch (JSONException e) {
                                    Log.e("createWhiteboard", "error parsing received message");
                                }
                                socketService.clearListener(SocketService.Messages.CREATE_WHITEBOARD);
                            }
                        });

                        fetchWhiteboardlist(); // refresh the list of whiteboards
                    }
                });
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.show();
            }
        });

        //Toggle off the fab.
        MainActivity activity = (MainActivity)getActivity();
        activity.toggleFABVisibility(false);
        return view;
    }


    /**
     * Called after onCreateView.  Important because setupContactListView() requires that the
     * view has been created and set.
     * @param view The view created in onCreateView.
     * @param savedInstanceState A previous instance to revert to.
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
    @TargetApi(Build.VERSION_CODES.M)
    private void setupWhiteboardListView() {
        //First get some strings
        ContactWb[] people = new ContactWb[whiteboardList.getSize()];

        //Pull the contact list in.
        for(int i = 0; i < whiteboardList.getSize(); i++) {
            people[i] = whiteboardList.getContactOrdinal(i);
        }

        try {
            ListAdapter customAdapter = new ContactListAdapter(this.getContext(), people);

            //Grab the list view and set the adapter.

            ListView listView = (ListView) getView().findViewById(R.id.contact_listview);
            listView.setAdapter(customAdapter);
            listView.setOnItemClickListener(makeWhiteboardListListener());

        } catch (NullPointerException ex) {
            Log.i("JOINBOARDFRAGMENT", ex.getMessage());
        }
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
                final ContactWb person = (ContactWb) parent.getItemAtPosition(position);
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
                        final JSONObject recvd = (JSONObject)args[0];
                        try {
                            Log.i("joinWhiteboard", "received message: " + recvd.getString("message"));
                            //Set the toolbar text
                            final MainActivity mainActivity = (MainActivity) getActivity();
                            mainActivity.runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    try {
                                        mainActivity.setTitleBarText(person.getName());
                                    } catch (Exception e) {

                                    }
                                }

                            });
                            //Fragment suicides here
                            Globals.getInstance().setWhiteboard(new Whiteboard(person.getName()));
                            mainActivity.onFragmentSuicide(SuicidalFragment.POP_ME);
                        } catch (JSONException e) {
                            Log.w("joinWhiteboard", "error parsing received message");
                        }
                        socket.clearListener(SocketService.Messages.JOIN_WHITEBOARD);
                    }
                });

                try {
                    socket.sendMessage(SocketService.Messages.JOIN_WHITEBOARD, joinWbRequest);
                } catch (Exception e) {
                    e.printStackTrace();
                }

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

        Globals.getInstance().refreshCurrentWhiteboard();

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(getActivity());
        String url = Globals.getInstance().getServerAddress() + "/whiteboards.json";
        Logger.getAnonymousLogger().info(url);


        // Request a string response from the provided URL.
        JsonArrayRequest jsonRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        // clear whiteboard list before adding them again
                        whiteboardList = new ContactList();
                        for(int i = 0; i < response.length(); i++) {
                            try {
                                String whiteboard = response.getString(i);
                                if(whiteboard.equals(Globals.getInstance().getCurrentWhiteboard()))
                                    whiteboardList.addContact(whiteboard, whiteboard, ContactList.STATUS_CONNECTED);
                                else
                                    whiteboardList.addContact(whiteboard, whiteboard, ContactList.STATUS_DISCONNECTED);

                            } catch (JSONException e) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @TargetApi(Build.VERSION_CODES.M)
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
                    @TargetApi(Build.VERSION_CODES.M)
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
