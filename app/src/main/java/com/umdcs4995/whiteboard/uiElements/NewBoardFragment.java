package com.umdcs4995.whiteboard.uiElements;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.umdcs4995.whiteboard.Globals;
import com.umdcs4995.whiteboard.R;
import com.umdcs4995.whiteboard.services.SocketService;

import org.json.JSONException;
import org.json.JSONObject;

import io.socket.emitter.Emitter;

/*
 * DialogFragment for creating a new whiteboard
 */
public class NewBoardFragment extends DialogFragment {

    /**
     * Empty constructor required for DialogFragment
     */
    public NewBoardFragment() {}

    private SocketService socketService = Globals.getInstance().getSocketService();

    /*
     * Creates the Dialog popup using an AlertDialog builder.
     */
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.fragment_new_whiteboard, null);
        builder.setView(dialogView);

        final EditText whiteboardName = (EditText) dialogView.findViewById(R.id.txt_board_name);

        builder.setTitle(R.string.dialog_new_whiteboard_title);
        builder.setPositiveButton(R.string.create, new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       JSONObject addWbRequest = new JSONObject();
                       try {
                           addWbRequest.put("name", whiteboardName.getText());
                       } catch (JSONException e) {
                           Toast.makeText(Globals.getInstance().getGlobalContext(), "Error creating whiteboard", Toast.LENGTH_LONG);
                       }

                       socketService.sendMessage(SocketService.Messages.CREATE_WHITEBOARD, addWbRequest);

                       socketService.addListener(SocketService.Messages.CREATE_WHITEBOARD, new Emitter.Listener() {
                           @Override
                           public void call(Object... args) {
                               JSONObject recvd = (JSONObject) args[0];
                               try {
                                   Log.i("createWhiteboard", "received message: " + recvd.getString("message"));
                               } catch (JSONException e) {
                                   Log.e("createWhiteboard", "error parsing received message");
                               }
                               socketService.clearListener(SocketService.Messages.CREATE_WHITEBOARD);
                           }
                       });
                   }
               });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {

                   }
               });

        return builder.create();
    }

    /*
     * Creates the DialogFragment for the MainActivity
     */
    public static NewBoardFragment newInstance() {
        return new NewBoardFragment();
    }
}