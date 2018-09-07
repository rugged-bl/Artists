package com.example.artists.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.artists.R;
import com.example.artists.fragment.artists_list.ArtistsListPresenter;
import com.example.artists.model.Artist;
import com.example.artists.model.EventEnumPublish;
import com.example.artists.model.FormatString;

import java.util.ArrayList;

import rx.Observable;

import static com.example.artists.AppConfig.COVER_SIZE_SMALL;

public class ArtistsListAdapter extends RecyclerView.Adapter<ArtistsListAdapter.TaskViewHolder> {
    // Tag used for debugging/logging
    public static final String TAG = "ArtistsListAdapter";
    // Constants used to improve performance
    private final String ALBUM_STR;
    private final String TRACK_STR;
    private final String ALBUMS_DELIMITER;

    private final Context context;
    private final LayoutInflater inflater;
    private final int actionLayout;
    private ArtistsListPresenter artistsListPresenter;

    public ArtistsListAdapter(ArtistsListPresenter artistsListPresenter, Context context) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.actionLayout = R.layout.single_list_item_artist;
        this.artistsListPresenter = artistsListPresenter;

        ALBUM_STR = context.getString(R.string.album_str);
        TRACK_STR = context.getString(R.string.track_str);
        ALBUMS_DELIMITER = context.getString(R.string.albums_delimiter_list);
    }

    @Override
    public TaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = inflater.inflate(actionLayout, parent, false);

        return new TaskViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final TaskViewHolder holder, int position) {
        Observable<Object> itemObs = getItem(position);

        // fill the fields of one list item
        itemObs.cast(Artist.class)
                .subscribe(artist -> {
                    artistsListPresenter.displayImage(artist.getCover().get(COVER_SIZE_SMALL), holder.iv_item_artist);
                    holder.tv_item_header.setText(artist.getName());
                    holder.tv_item_albums.setText(
                            FormatString.formatAlbDeclination(artist.getAlbums(), ALBUM_STR) +
                                    ALBUMS_DELIMITER +
                                    FormatString.formatAlbDeclination(artist.getTracks(), TRACK_STR));

                    ArrayList<String> genres = artist.getGenres();
                    if (genres.isEmpty()) {
                        holder.tv_item_genre.setVisibility(View.GONE);
                    } else {
                        holder.tv_item_genre.setText(FormatString.formatGenres(genres));
                        holder.tv_item_genre.setVisibility(View.VISIBLE);
                    }

                    // sets the onClickListener which emits REPLACE_FRAGMENT message
                    // in an intention to change fragment to Detail
                    holder.itemView.setOnClickListener(v -> EventEnumPublish.REPLACE_FRAGMENT.publish(artist));
                });
    }

    @Override
    public int getItemCount() {
        return artistsListPresenter.getItemCount();
    }

    private Observable<Object> getItem(int position) {
        return artistsListPresenter.getItem(position);
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        public ImageView iv_item_artist;
        public TextView tv_item_header;
        public TextView tv_item_genre;
        public TextView tv_item_albums;

        TaskViewHolder(View itemView) {
            super(itemView);
            iv_item_artist = (ImageView) itemView.findViewById(R.id.iv_item_artist);
            tv_item_header = (TextView) itemView.findViewById(R.id.tv_item_header);
            tv_item_genre = (TextView) itemView.findViewById(R.id.tv_item_genre);
            tv_item_albums = (TextView) itemView.findViewById(R.id.tv_item_albums);
        }
    }
}