package com.umdcs4995.whiteboard.uiElements;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.umdcs4995.whiteboard.Globals;
import com.umdcs4995.whiteboard.R;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.logging.Logger;

import contacts.ContactList;
import contacts.ContactListAdapter;
import contacts.ContactWb;

/*
 * Fragment for creating a new whiteboard
 */
public class NewBoardFragment extends Fragment {

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
     * Make an item click listener for the contacts.
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



}