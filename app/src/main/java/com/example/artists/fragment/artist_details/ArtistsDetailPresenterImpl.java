package com.example.artists.fragment.artist_details;

import android.support.annotation.NonNull;
import android.widget.ImageView;

import com.example.artists.App;
import com.example.artists.R;
import com.example.artists.model.FormatString;
import com.example.artists.model.MainModel;
import com.example.artists.model.MainModelImpl;

import java.util.ArrayList;

public class ArtistsDetailPresenterImpl implements ArtistsDetailPresenter {
    // Tag used for debugging/logging
    public static final String TAG = "ArtistsListPresenterImp";

    private ArtistsDetailView artistsDetailView;
    private MainModel mainModel;

    ArtistsDetailPresenterImpl(ArtistsDetailView artistsDetailView) {
        this.artistsDetailView = artistsDetailView;
        this.mainModel = MainModelImpl.INSTANCE;
    }

    public String getAlbums(int albums, int tracks) {
        return FormatString.formatAlbDeclination(albums, App.getContext().getString(R.string.album_str)) +
                App.getContext().getString(R.string.albums_delimiter_detail) +
                FormatString.formatAlbDeclination(tracks, App.getContext().getString(R.string.track_str));
    }

    public String getGenres(ArrayList<String> genres) {
        return FormatString.formatGenres(genres);
    }

    public String getBioDescription(String description) {
        return FormatString.startWithUpperCase(description);
    }

    public void displayImage(String uri, @NonNull ImageView imageView) {
        mainModel.displayImage(uri, imageView);
    }
}
