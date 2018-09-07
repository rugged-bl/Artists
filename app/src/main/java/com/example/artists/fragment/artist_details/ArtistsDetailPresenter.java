package com.example.artists.fragment.artist_details;

import android.support.annotation.NonNull;
import android.widget.ImageView;

import java.util.ArrayList;

public interface ArtistsDetailPresenter {
    String getAlbums(int albums, int tracks);

    String getBioDescription(String description);

    String getGenres(ArrayList<String> genres);

    void displayImage(String uri, @NonNull ImageView imageView);
}
