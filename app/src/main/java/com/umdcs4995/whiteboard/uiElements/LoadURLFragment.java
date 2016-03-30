package com.umdcs4995.whiteboard.uiElements;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.umdcs4995.whiteboard.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link LoadURLFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link LoadURLFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LoadURLFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private OnOkBtnClickedListener okBtnListener;

    public LoadURLFragment() {
        // Required empty public constructor
    }

    public interface OnOkBtnClickedListener{
        public void onOkBtnClicked(String urlString);
    }


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment LoadURLFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LoadURLFragment newInstance(String param1, String param2) {
        LoadURLFragment fragment = new LoadURLFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    /**
     * Note that onCreate methods are called on actual instantiation of the object by an intent.
     * and not on instantiation or activation of the fragment.
     *
     * In general, don't put things in the onCreate method.
     */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_load_url, container, false);
        // Inflate the layout for this fragment
        final EditText editText = (EditText) rootView.findViewById(R.id.editText);
        Button OKBtn = (Button) rootView.findViewById(R.id.btn_ok);
        OKBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Probably pass string for url back to main program
                //note: maybe add a check to see if the strings at least end in something valid?
                if(okBtnListener != null) {
                    okBtnListener.onOkBtnClicked(editText.getText().toString());
                }

            }
        });


        return rootView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);

        }
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
        if (context instanceof OnOkBtnClickedListener) {
            okBtnListener = (OnOkBtnClickedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnOkBtnClickedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
