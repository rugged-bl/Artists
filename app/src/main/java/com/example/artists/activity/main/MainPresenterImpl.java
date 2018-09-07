package com.example.artists.activity.main;

import com.example.artists.model.MainModel;
import com.example.artists.model.MainModelImpl;

public class MainPresenterImpl implements MainPresenter {
    public static final String TAG = "ArtistsListPresenterImpl";

    private MainView mainView;
    private MainModel mainModel;

    MainPresenterImpl(MainView mainView) {
        this.mainView = mainView;
        this.mainModel = MainModelImpl.INSTANCE;
    }
}
