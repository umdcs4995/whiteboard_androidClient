package com.umdcs4995.whiteboard.uiElements;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
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
import com.umdcs4995.whiteboard.whiteboarddata.GoogleUser;

/**
 * Created by rob on 2/14/16.
 */
public class BuddyListAdapter extends ArrayAdapter<GoogleUser> {


    public BuddyListAdapter(Context context, GoogleUser[] buddies) {
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
        GoogleUser buddy = getItem(position);

        //Sets the users name status and icon
        TextView nameView = (TextView) customView.findViewById(R.id.textView_buddylist_name);
        TextView emailView = (TextView) customView.findViewById(R.id.textview_buddylist_email);
        ImageView profilePic = (ImageView) customView.findViewById(R.id.imageView_buddyList_profile_pic);

        //Set the fields with the buddy's information.
        nameView.setText(buddy.getFullname());
        emailView.setText(buddy.getEmail());

        Bitmap bm = buddy.getRoundedProfileImage(70);
        profilePic.setImageBitmap(bm);

        //After making changes we need to return the view
        return customView;
    }

}
