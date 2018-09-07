package com.example.artists.model;

import android.support.annotation.NonNull;
import android.widget.ImageView;

import java.util.Collection;

import rx.Observable;

public interface MainModel {
    void addArtist(Artist artist);

    void addAllArtists(Collection<? extends Artist> collection);

    void displayImage(String uri, @NonNull ImageView imageView);

    int getItemCount();

    Observable<Object> getItem(int position);
}
