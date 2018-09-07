package com.example.artists.fragment.artists_list;

import android.support.annotation.NonNull;
import android.widget.ImageView;

import rx.Observable;

public interface ArtistsListPresenter {
    int getItemCount();

    Observable<Object> getItem(int position);

    void displayImage(String uri, @NonNull ImageView imageView);
}
