package contacts;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.umdcs4995.whiteboard.R;

/**
 * Created by rob on 2/14/16.
 */
public class ContactListAdapter extends ArrayAdapter<ContactWb> {


    public ContactListAdapter(Context context, ContactWb[] contacts) {
        //Automatically generated constructor with alt+insert
        //change "int resource" to Typename[] name and then call super
        //on context, new layout, name)
        super(context, R.layout.contactlist_custom_row, contacts);
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
        View customView = inflater.inflate(R.layout.contactlist_custom_row, parent, false);

        //Retrieving the contact info from the position value passed in.
        ContactWb contact = getItem(position);

        //Sets the users name status and icon
        TextView nameView = (TextView) customView.findViewById(R.id.textView_contactlist_name);
        TextView statusView = (TextView) customView.findViewById(R.id.textview_contactlist_status);
        ImageView statusIcon = (ImageView) customView.findViewById(R.id.imageView_contactList_connection_icon);

        nameView.setText(contact.getName());
        //Checking to see if the user is connected
        if(contact.getStatus() == ContactList.STATUS_CONNECTED) {
            //If connected we change the text to indicate so.
            statusView.setText("Connected");
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
}
