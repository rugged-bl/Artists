package com.example.artists.model;

import java.util.ArrayList;
import java.util.HashMap;

public class Artist {
    private final int id;
    private final String name;
    private final ArrayList<String> genres;
    private final int tracks;
    private final int albums;
    private final String link;
    private final String description;
    private final HashMap<String, String> cover;

    Artist(int id, String name, ArrayList<String> genres, int tracks, int albums,
           String link, String description, HashMap<String, String> cover) {
        this.id = id;
        this.name = name;
        this.genres = genres;
        this.tracks = tracks;
        this.albums = albums;
        this.link = link;
        this.description = description;
        this.cover = cover;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ArrayList<String> getGenres() {
        return genres;
    }

    public int getTracks() {
        return tracks;
    }

    public int getAlbums() {
        return albums;
    }

    public String getLink() {
        return link;
    }

    public String getDescription() {
        return description;
    }

    public HashMap<String, String> getCover() {
        return cover;
    }

}
