package com.umdcs4995.whiteboard.uiElements;

import android.app.Fragment;

/**
 * Interface allows fragments to suicide themselves.  Allows fragments to tell an activity that it
 * should close them.
 * Created by rob on 4/8/16.
 */
public interface SuicidalFragment {
    public static int POP_ME = 0;

    //Called by fragments when they need to kill themselves.
    void onFragmentSuicide(int command);
}
