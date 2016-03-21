package com.umdcs4995.whiteboard.uiElements;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.umdcs4995.whiteboard.R;

import contacts.ContactList;
import contacts.ContactListAdapter;
import contacts.ContactWb;

/**
 * Activity for handling the contact list screen for the app.
 */

public class ContactListFragment extends Fragment {

    ContactList contactList = new ContactList();

    /**
     * Called on creation of the fragment.
     * @param savedInstanceState
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        setupTestContacts();

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
        setupContactListView();
    }

    /**
     * Briefly setup some test contacts.
     */
    private void setupTestContacts() {
        contactList.addContact("rob0001", "Rob", ContactList.STATUS_CONNECTED);
        contactList.addContact("pet0001", "Peter", ContactList.STATUS_CONNECTED);
        contactList.addContact("willems", "Willemsen", ContactList.STATUS_DISCONNECTED);
        contactList.addContact("lau0001", "Laura", ContactList.STATUS_CONNECTED);
        contactList.addContact("kad0001", "Kade", ContactList.STATUS_DISCONNECTED);
        contactList.addContact("ama0001", "Amanda", ContactList.STATUS_DISCONNECTED);
        contactList.addContact("sco0001", "Scott", ContactList.STATUS_CONNECTED);
        contactList.addContact("jes0001", "Jesse", ContactList.STATUS_DISCONNECTED);
        contactList.addContact("tri0001", "Tristan", ContactList.STATUS_DISCONNECTED);
        contactList.addContact("mit0001", "Mitch", ContactList.STATUS_CONNECTED);
        contactList.addContact("maz0001", "Maz", ContactList.STATUS_DISCONNECTED);
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
