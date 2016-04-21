package com.umdcs4995.whiteboard.uiElements;

import android.app.AlertDialog;
import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.umdcs4995.whiteboard.Globals;
import com.umdcs4995.whiteboard.R;
import com.umdcs4995.whiteboard.services.SocketService;

import org.json.JSONException;
import org.json.JSONObject;

import contacts.ContactWb;
import io.socket.emitter.Emitter;

/**
 * Created by goebe076 on 4/17/2016.
 */
public class DeleteBoardFragment extends DialogFragment {

    private SocketService socketService = Globals.getInstance().getSocketService();
    private DialogClickListener callback;

    public DeleteBoardFragment() {
        // empty constructor required
    }

    public static DeleteBoardFragment newInstance(int num) {
        DeleteBoardFragment fragment = new DeleteBoardFragment();
        Bundle args = new Bundle();
        args.putInt("num", num);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            callback = (DialogClickListener) getTargetFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException("Calling fragment must implement DialogClickListener interface");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final String board = getArguments().getString("name");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.dialog_delete_whiteboard);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            /**
             * When user clicks "Yes", alert the server to delete the whiteboard.
             * @param dialog The dialog that received the click.
             * @param which The number of the button being pressed.
             */
            @Override
            public void onClick(DialogInterface dialog, int which) {
                callback.onPositiveClick();
//                JSONObject deleteRequest = new JSONObject();
//
//                // Put whiteboard name into JSONObject
//                try {
//                    deleteRequest.put("name", board);
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//
//                // Deliver "delete whiteboard" message to server
//                try {
//                    socketService.sendMessage(SocketService.Messages.DELETE_WHITEBOARD, deleteRequest);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//
//                // Toast message to test button functionality
//                Toast.makeText(getContext(), "Whiteboard deleted", Toast.LENGTH_SHORT).show();
//                Log.i("deleteWhiteboard", "whiteboard deleted");
            }
        });
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                callback.onNegativeClick();
            }
        });

        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                dialog.cancel();
            }
        });

        Dialog dialog = builder.create();

        return dialog;

//        return builder.create();
    }

}
