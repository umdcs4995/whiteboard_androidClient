package com.umdcs4995.whiteboard;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import contacts.ContactList;
import contacts.ContactListAdapter;
import contacts.ContactWb;
import uiFragments.NotYetImplementedToast;

public class ContactListActivity extends AppCompatActivity {

    ContactList contactList = new ContactList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) { //Set the title.
            getSupportActionBar().setTitle("Contacts");
        }

        setupTestContacts();
        setupContactListView();
//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
    }

    /**
     * This method override creates the settings menu (the three dots in the upper right corner.
     * It's called automatically after onCreate(..).  To see the xml code for the menu, see the
     * menu_mainoverflow.xml file.
     */
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_mainoverflow, menu);
        return true;
    }

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

    private void setupContactListView() {
        //First get some strings
        ContactWb[] people = new ContactWb[contactList.getSize()];

        //Pull the contact list in.
        for(int i = 0; i < contactList.getSize(); i++) {
            people[i] = contactList.getContactOrdinal(i);
        }

        ListAdapter customAdapter = new ContactListAdapter(this, people);
        //Grab the list view and set the adapter.
        ListView listView = (ListView) findViewById(R.id.contact_listview);
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
                new NotYetImplementedToast(ContactListActivity.this, person.getName() + " clicked!");
            }
        };

        return l;
    }

}
