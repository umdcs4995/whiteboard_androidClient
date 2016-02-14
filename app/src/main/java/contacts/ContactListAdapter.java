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

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //This is where you tell it where to put
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View customView = inflater.inflate(R.layout.contactlist_custom_row, parent, false);

        ContactWb contact = getItem(position);
        TextView nameView = (TextView) customView.findViewById(R.id.textView_contactlist_name);
        TextView statusView = (TextView) customView.findViewById(R.id.textview_contactlist_status);
        ImageView statusIcon = (ImageView) customView.findViewById(R.id.imageView_contactList_connection_icon);

        nameView.setText(contact.getName());
        if(contact.getStatus() == ContactList.STATUS_CONNECTED) {
            statusView.setText("Connected");
            statusIcon.setImageResource(R.drawable.connected);
            statusView.setTextColor(Color.rgb(0,200,50));
        } else {
            statusView.setText("Disconnected");
            statusIcon.setImageResource(R.drawable.disconnected);
            statusView.setTextColor(Color.rgb(200,0,0));
        }

        return customView;
    }
}
