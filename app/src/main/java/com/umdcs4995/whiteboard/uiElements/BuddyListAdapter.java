package com.umdcs4995.whiteboard.uiElements;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.umdcs4995.whiteboard.R;
import com.umdcs4995.whiteboard.whiteboarddata.Buddy;

/**
 * Created by rob on 2/14/16.
 */
public class BuddyListAdapter extends ArrayAdapter<Buddy> {


    public BuddyListAdapter(Context context, Buddy[] buddies) {
        //Automatically generated constructor with alt+insert
        //change "int resource" to Typename[] name and then call super
        //on context, new layout, name)
        super(context, R.layout.buddylist_custom_row, buddies);
    }
    /**
     * Returns the view after checking if a user is connected or disconnected.
     * @param position - the position associated with a contact
     * @return customView
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //This is where you tell it where to put
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View customView = inflater.inflate(R.layout.buddylist_custom_row, parent, false);

        //Retrieving the contact info from the position value passed in.
        Buddy buddy = getItem(position);

        //Sets the users name status and icon
        TextView nameView = (TextView) customView.findViewById(R.id.textView_contactlist_name);
        TextView statusView = (TextView) customView.findViewById(R.id.textview_contactlist_status);
        ImageView statusIcon = (ImageView) customView.findViewById(R.id.imageView_contactList_connection_icon);
        ImageView deleteButton = (ImageView) customView.findViewById(R.id.imageButton_contactlist_delete);

        nameView.setText(buddy.getFullname());
        //Checking to see if the user is connected
        if(buddy.isLoggedIn()) {
            //If connected we change the text to indicate so.
            statusView.setText("Logged In");
            statusIcon.setImageResource(R.drawable.connected);
            //We also change the color to green indicating they are connected.
            statusView.setTextColor(Color.rgb(0,200,50));
        } else {
            //If disconnected we change the text to say so.
            statusView.setText("Disconnected");
            statusIcon.setImageResource(R.drawable.disconnected);
            //Again we change the color to indicate the disconnection.
            statusView.setTextColor(Color.rgb(200,0,0));
        }
        //After making changes we need to return the view
        return customView;
    }

    /**
     * Completes action for "yes" clicked on delete board dialog.
     */
    @TargetApi(Build.VERSION_CODES.M)
    public void onPositiveClick() {
        Toast.makeText(getContext(), "Whiteboard deleted", Toast.LENGTH_SHORT).show();
        Log.i("DeleteBoardDialog", "'Yes' clicked.");
    }

    /**
     * Completes action for "no" clicked on delete board dialog.
     */
    public void onNegativeClick() {
        Toast.makeText(getContext(), "Whiteboard not deleted", Toast.LENGTH_SHORT).show();
        Log.i("DeleteBoardDialog", "'No' clicked.");
    }
}
