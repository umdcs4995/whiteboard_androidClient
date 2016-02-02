package uiFragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.umdcs4995.whiteboard.R;

/**
 * Fragment for the About button press.  Returns a simple dialog box.
 */
public class AboutDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //Builder creates a view to look at.
        //AlertDialog.Builder means it builds an alert dialog, similar to a Java popup.
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        //Title and String information.
        builder.setTitle("About Whiteboard");
        String str = new String();

        str += ("Whiteboard \n");
        str += ("Version: 0.0\n");
        str += ("Release Date: " + getString(R.string.release_date) + "\n\n");
        str += (getString(R.string.authors));

        //Set the message of the builder.
        builder.setMessage(str);

        //What to do on an ok press.
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //dismiss the dialog.
                dismiss();
            }
        });

        //Returns the dialog.
        return builder.create();
    }
}
