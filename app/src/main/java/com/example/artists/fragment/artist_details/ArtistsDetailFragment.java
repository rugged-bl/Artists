package com.example.artists.fragment.artist_details;

import android.content.Context;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.artists.R;
import com.example.artists.activity.main.MainActivity;
import com.example.artists.model.Artist;
import com.example.artists.model.EventEnumBehavior;

import java.util.ArrayList;

import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

import static com.example.artists.AppConfig.COVER_SIZE_BIG;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ArtistsDetailFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ArtistsDetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ArtistsDetailFragment extends Fragment implements ArtistsDetailView {
    // Tag used for debugging/logging
    public static final String TAG = "ArtistsDetailFragment";

    private OnFragmentInteractionListener mListener;

    // CompositeSubscription used for managing subscriptions
    private CompositeSubscription compositeSubscription;
    private ArtistsDetailPresenter presenter;

    private CollapsingToolbarLayout collapsingToolbarLayout;

    private ImageView iv_item_artist;
    private TextView tv_item_genre;
    private TextView tv_item_albums;
    private TextView tv_item_bio_header;
    private TextView tv_item_bio_description;

    public ArtistsDetailFragment() {
        compositeSubscription = new CompositeSubscription();
    }

    public static ArtistsDetailFragment newInstance() {
        return new ArtistsDetailFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        presenter = new ArtistsDetailPresenterImpl(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_detail_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        iv_item_artist = (ImageView) view.findViewById(R.id.iv_item_artist);
        tv_item_genre = (TextView) view.findViewById(R.id.tv_item_genre);
        tv_item_albums = (TextView) view.findViewById(R.id.tv_item_albums);
        tv_item_bio_header = (TextView) view.findViewById(R.id.tv_item_bio_header);
        tv_item_bio_description = (TextView) view.findViewById(R.id.tv_item_bio_description);

        //YoYo.with(Techniques.FadeIn).duration(1500).playOn(iv_item_artist);

        collapsingToolbarLayout = (CollapsingToolbarLayout) view.findViewById(R.id.collapsing_toolbar);
        collapsingToolbarLayout.setTitle(getContext().getString(R.string.detail_title_default_str));
        // collapsingToolbarLayout.setExpandedTitleColor(ContextCompat.getColor(getContext(), android.R.color.transparent));

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        ((MainActivity) getActivity()).setSupportActionBar(toolbar);
        ActionBar actionBar = ((MainActivity) getActivity()).getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        /**
         * Subscribe used for both receiving artist information and showing it in a Detailed View
         * All strings are formed by ArtistsDetailPresenterImpl
         * AppBarLayout toggles to collapsed in landscape orientation
         * Errors been caught and printed locally ;
         */
        compositeSubscription.add(
                EventEnumBehavior.DETAILS_FRAGMENT_INFO.subscribe()
                        .observeOn(AndroidSchedulers.mainThread())
                        .cast(Artist.class)
                        .subscribe(artist -> {
                            Log.d(TAG, "DETAILS_FRAGMENT_INFO msg got");

                            presenter.displayImage(artist.getCover().get(COVER_SIZE_BIG), iv_item_artist);
                            tv_item_bio_description.setText(presenter.getBioDescription(artist.getDescription()));

                            ArrayList<String> genres = artist.getGenres();
                            if (genres.isEmpty()) {
                                tv_item_genre.setVisibility(View.GONE);
                            } else {
                                tv_item_genre.setText(presenter.getGenres(genres));
                                tv_item_genre.setVisibility(View.VISIBLE);
                            }

                            tv_item_albums.setText(presenter.getAlbums(artist.getAlbums(), artist.getTracks()));

                            collapsingToolbarLayout.setTitle(artist.getName());

                            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                                AppBarLayout appBarLayout = ((AppBarLayout) view.findViewById(R.id.app_bar_layout));
                                appBarLayout.setExpanded(false, true);
                            }
                        }, Throwable::printStackTrace));

        //mListener.onFragmentInteraction(Uri.EMPTY);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (compositeSubscription != null && compositeSubscription.hasSubscriptions()) {
            compositeSubscription.clear();
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
    }

    @Override
    public void onDetach() {
        super.onDetach();

        Log.d(TAG, "onDetach");
        if (compositeSubscription != null && !compositeSubscription.isUnsubscribed()) {
            compositeSubscription.unsubscribe();
        }
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
