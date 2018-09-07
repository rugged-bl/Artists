package com.example.artists.model;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.ImageView;

import com.example.artists.App;
import com.example.artists.AppConfig;
import com.example.artists.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import rx.Observable;
import rx.Subscription;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static com.example.artists.AppConfig.ARTISTS_LIST_ERROR;
import static com.example.artists.AppConfig.ARTISTS_LIST_SUCCESS;
import static com.example.artists.AppConfig.ARTISTS_LIST_UNHANDLED;

public class MainModelImpl implements MainModel {
    // Tag used for debugging/logging
    public static final String TAG = "MainModelImpl";

    // a simple static-field singleton
    public static final MainModelImpl INSTANCE = new MainModelImpl();
    private CopyOnWriteArrayList<Artist> artists = new CopyOnWriteArrayList<>();
    //private AtomicInteger itemsCount = new AtomicInteger(0);

    private ImageLoader imageLoader;
    private DisplayImageOptions displayImageOptions;

    // CompositeSubscription used for managing subscriptions
    private CompositeSubscription compositeSubscription;

    private MainModelImpl() {
        EventEnumBehavior.HANDLE_ARTISTS.publish(ARTISTS_LIST_UNHANDLED);

        Schedulers.computation().createWorker().schedule(() -> {
            ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(App.getContext())
                    .build();
            ImageLoader.getInstance().init(config);

            // building the DisplayImage options
            displayImageOptions = new DisplayImageOptions.Builder()
                    .showImageOnLoading(R.drawable.empty_grey)
                    .showImageForEmptyUri(R.drawable.ic_block_black)
                    .showImageOnFail(R.drawable.ic_block_black)
                    .cacheInMemory(true)
                    .cacheOnDisk(true)
                    .build();

            compositeSubscription = new CompositeSubscription();

            // starting the WebService with json url as a parameter
            Intent intent = new Intent(App.getContext(), WebService.class);
            intent.putExtra(WebService.URL_FILE_KEY, AppConfig.URL_JSON_FILE);
            App.getContext().startService(intent);

            handleArtistsFile();
        });
    }

    /**
     * Looks for successful json, then parse it into an artists array
     * and emitting a state if array if successfully filled or not
     */
    private void handleArtistsFile() {
        Subscription subscription = EventEnumBehavior.PUBLISH_FILE.subscribe()
                .observeOn(Schedulers.computation())
                .subscribeOn(Schedulers.computation())
                .cast(String.class)
                .subscribe(s -> {
                    try {
                        JSONArray jsonArtists = new JSONArray(s);
                        parseAddArtists(jsonArtists);
                        EventEnumBehavior.HANDLE_ARTISTS.publish(ARTISTS_LIST_SUCCESS);
                    } catch (JSONException e) {
                        EventEnumBehavior.HANDLE_ARTISTS.publish(ARTISTS_LIST_ERROR);
                        e.printStackTrace();
                    }
                    EventEnumBehavior.HANDLE_ARTISTS.subscribe().onCompleted();
                });
        compositeSubscription.add(subscription);
    }

    private void parseAddArtists(JSONArray jsonArray) {
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject object = jsonArray.getJSONObject(i);

                int id = object.getInt("id");
                String name = object.getString("name");
                JSONArray genres = object.getJSONArray("genres");
                int tracks = object.getInt("tracks");
                int albums = object.getInt("albums");
                String link = "";
                try {
                    link = object.getString("link");
                } catch (JSONException e) {
                    //велосипед
                    Log.w(TAG, e.getMessage());
                }
                String description = object.getString("description");
                JSONObject cover = object.getJSONObject("cover");
                String small = cover.getString("small");
                String big = cover.getString("big");

                ArrayList<String> genresList = new ArrayList<>();
                for (int j = 0; j < genres.length(); j++)
                    genresList.add(genres.getString(j));

                HashMap<String, String> coverHashMap = new HashMap<>();
                coverHashMap.put("small", small);
                coverHashMap.put("big", big);

                addArtist(new Artist(id, name, genresList, tracks, albums, link, description, coverHashMap));
            } catch (JSONException e) {
                e.printStackTrace();
                //Log.w(TAG, e.getMessage());
            }
        }
    }

    public void addArtist(Artist artist) {
        artists.addIfAbsent(artist);
    }

    public void addAllArtists(Collection<? extends Artist> collection) {
        artists.addAllAbsent(collection);
    }

    public void removeArtist(Artist artist) {
        artists.remove(artist);
    }

    public void displayImage(String uri, @NonNull ImageView imageView) {
        if (imageLoader == null)
            imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(uri, imageView, displayImageOptions);
    }

    public int getItemCount() {
        return artists.size();
    }

    public Observable<Object> getItem(int position) {
        if (position < 0) throw new IllegalArgumentException("position must be greater than zero");

        return Observable.just(artists.get(position));
    }
}
