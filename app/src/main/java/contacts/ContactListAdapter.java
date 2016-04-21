package contacts;

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
        ImageView deleteButton = (ImageView) customView.findViewById(R.id.imageButton_contactlist_delete);

        // Setup the Create New Whiteboard button
        deleteButton.setOnClickListener(new View.OnClickListener() {
                                            public void onClick(View v) {

                                                Activity act = (Activity) getContext();
                                                AlertDialog.Builder builder = new AlertDialog.Builder(act);

                                                builder.setTitle(R.string.dialog_delete_whiteboard);
                                                builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        Log.i("success!!!", "Clicked it!");
                                                    }
                                                });
                                                builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        Log.i("booo!!!", "Clicked it!");
                                                    }
                                                });
                                                builder.show();
                                            }
                                        });

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
