package com.example.artists.fragment.artists_list;

import android.support.annotation.NonNull;
import android.widget.ImageView;

import com.example.artists.model.EventEnumBehavior;
import com.example.artists.model.MainModel;
import com.example.artists.model.MainModelImpl;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

import static com.example.artists.AppConfig.ARTISTS_LIST_ERROR;
import static com.example.artists.AppConfig.ARTISTS_LIST_SUCCESS;
import static com.example.artists.AppConfig.ARTISTS_LIST_UNHANDLED;

public class ArtistsListPresenterImpl implements ArtistsListPresenter {
    // Tag used for debugging/logging
    public static final String TAG = "ArtistsListPresenterImp";

    // contain state of filling the list
    private static volatile Integer listFilled;

    private ArtistsListView artistsListView;
    private MainModel mainModel;

    ArtistsListPresenterImpl(ArtistsListView artistsListView) {
        this.artistsListView = artistsListView;
        this.mainModel = MainModelImpl.INSTANCE;

        // setting the subscription for managing if artists are
        // successfully loaded or not;
        updateListFilledHandler();
    }

    private void updateListFilledHandler() {
        EventEnumBehavior.HANDLE_ARTISTS.subscribe()
                .observeOn(AndroidSchedulers.mainThread())
                .cast(Integer.class)
                .subscribe(integer ->
                {
                    listFilled = integer;
                    updateListFilled(listFilled);
                });
    }

    /**
     * calling the artists view to show correct state
     *
     * @param state state of artists list been successfully loaded or not
     */
    private void updateListFilled(int state) {
        switch (state) {
            case ARTISTS_LIST_UNHANDLED:
                artistsListView.setLoadingState();
                break;
            case ARTISTS_LIST_ERROR:
                artistsListView.setErrorState();
                break;
            case ARTISTS_LIST_SUCCESS:
                artistsListView.setSuccessState();
        }
    }

    public int getItemCount() {
        return mainModel.getItemCount();
    }

    public Observable<Object> getItem(int position) {
        return mainModel.getItem(position);
    }

    public void displayImage(String uri, @NonNull ImageView imageView) {
        mainModel.displayImage(uri, imageView);
    }
}
