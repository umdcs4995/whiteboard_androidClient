package com.umdcs4995.whiteboard.uiElements;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.umdcs4995.whiteboard.Globals;
import com.umdcs4995.whiteboard.R;
import com.umdcs4995.whiteboard.services.SocketService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.logging.Logger;

import contacts.ContactList;
import contacts.ContactListAdapter;
import contacts.ContactWb;
import io.socket.emitter.Emitter;

/*
 * DialogFragment for creating a new whiteboard
 */
public class NewBoardFragment extends DialogFragment {

    private SocketService socketService = Globals.getInstance().getSocketService();

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.dialog_add_board).setPositiveButton(R.string.create, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User tries to create a new whiteboard
                JSONObject createWbRequest = new JSONObject();
                try {
                    // TODO: make a whiteboard name chooser and use its input here
                    createWbRequest.put("name", "Test");
                } catch (JSONException e) {
                    Toast.makeText(Globals.getInstance().getGlobalContext(), "Error making createWhiteboard request - this is bad...", Toast.LENGTH_LONG);
                }
                socketService.sendMessage(SocketService.Messages.CREATE_WHITEBOARD, createWbRequest);

                socketService.addListener(SocketService.Messages.CREATE_WHITEBOARD, new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        // TODO: Set up the whiteboard + join it here
                        JSONObject recvd = (JSONObject) args[0];
                        try {
                            Log.i("createWhiteboard", "received message: " + recvd.getString("message"));
                        } catch (JSONException e) {
                            Log.w("createWhiteboard", "error parsing received message");
                        }
                        socketService.clearListener(SocketService.Messages.CREATE_WHITEBOARD);
                    }
                });
            }
        }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User cancelled the creation
            }
        });
        return builder.create();
    }

    public static NewBoardFragment newInstance() {
        return new NewBoardFragment();
    }
}