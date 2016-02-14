package com.umdcs4995.whiteboard;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import uiFragments.NotYetImplementedToast;

public class ContactListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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


    private void setupContactListView() {
        //First get some strings
        String[] people = {"Rob", "Peter", "Kade", "Laura"};

        //Convert the strings into a list adapter.  The second parameter is an android defined
        //default layout for list items.
        ListAdapter listAdapter = new ArrayAdapter<String>(this, //context of the activity.
                android.R.layout.simple_list_item_1, //Layout to use.
                people); //The array to convert.

        //Grab the list view and set the adapter.
        ListView listView = (ListView) findViewById(R.id.contact_listview);
        listView.setAdapter(listAdapter);
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
                String person = String.valueOf(parent.getItemAtPosition(position));
                new NotYetImplementedToast(ContactListActivity.this, person + " clicked!");
            }
        };

        return l;
    }

}
