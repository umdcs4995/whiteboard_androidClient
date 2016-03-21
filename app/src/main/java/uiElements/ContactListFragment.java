package uiElements;

import android.app.Fragment;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;

import contacts.ContactList;
import contacts.ContactListAdapter;
import contacts.ContactWb;
import uiElements.NotYetImplementedToast;

/**
 * Activity for handling the contact list screen for the app.
 */

public class ContactListFragment extends Fragment {

    ContactList contactList = new ContactList();

    /**
     * Called on creation of the fragment.
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setupTestContacts();
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

        //TO DO: Fix this line when creating a new layout fo the contact list.
        //Then remove assert.8
        assert (false);
        //ListView listView = (ListView) this.getActivity().findViewById(R.id.contact_listview);
        //listView.setAdapter(customAdapter);
        //listView.setOnItemClickListener(makeContactListListener());
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
