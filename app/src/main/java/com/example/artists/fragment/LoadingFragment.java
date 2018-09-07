package com.example.artists.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.artists.R;

import static com.example.artists.AppConfig.ARTISTS_LIST_ERROR;
import static com.example.artists.AppConfig.ARTISTS_LIST_UNHANDLED;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LoadingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LoadingFragment extends Fragment {
    // the fragment initialization parameters
    private static final String FRAGMENT_TYPE = "FRAGMENT_TYPE";

    // fragmentType can be error, success or unhandled
    private int fragmentType;

    public LoadingFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param fragmentType Fragment type: error, success, unhandled.
     * @return A new instance of fragment LoadingFragment.
     */
    public static LoadingFragment newInstance(int fragmentType) {
        LoadingFragment fragment = new LoadingFragment();
        Bundle args = new Bundle();
        args.putInt(FRAGMENT_TYPE, fragmentType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            fragmentType = getArguments().getInt(FRAGMENT_TYPE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // inflating appropriate view
        if (fragmentType == ARTISTS_LIST_UNHANDLED)
            return inflater.inflate(R.layout.fragment_loading, container, false);
        else if (fragmentType == ARTISTS_LIST_ERROR)
            return inflater.inflate(R.layout.fragment_error, container, false);
        else return inflater.inflate(R.layout.fragment_error, container, false);
    }
}
