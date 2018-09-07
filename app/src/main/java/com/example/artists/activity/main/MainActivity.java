package com.example.artists.activity.main;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.artists.R;
import com.example.artists.fragment.LoadingFragment;
import com.example.artists.fragment.artist_details.ArtistsDetailFragment;
import com.example.artists.fragment.artists_list.ArtistsListFragment;
import com.example.artists.model.Artist;
import com.example.artists.model.EventEnumBehavior;
import com.example.artists.model.EventEnumPublish;

import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

import static com.example.artists.AppConfig.ARTISTS_LIST_ERROR;
import static com.example.artists.AppConfig.ARTISTS_LIST_SUCCESS;
import static com.example.artists.AppConfig.ARTISTS_LIST_UNHANDLED;
import static com.example.artists.AppConfig.LIST_FRAGMENT_TYPE;

public class MainActivity extends AppCompatActivity implements
        ArtistsListFragment.OnFragmentInteractionListener,
        ArtistsDetailFragment.OnFragmentInteractionListener,
        MainView {

    // Tag used for debugging/logging
    public static final String TAG = "MainActivity";

    private FragmentManager.OnBackStackChangedListener
            mOnBackStackChangedListener = this::syncActionBarArrowState;

    private CompositeSubscription compositeSubscription;
    private MainPresenter mainPresenter;
    /*private ImageButton btnToolbarButton;
    Toolbar toolbar;*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        compositeSubscription = new CompositeSubscription();
        mainPresenter = new MainPresenterImpl(this);
        /*toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setTitle("Исполнители");
        }
        getSupportFragmentManager().addOnBackStackChangedListener(mOnBackStackChangedListener);

        if (toolbar != null) { //Анимация
            try {
                Field fNavBtn = toolbar.getClass().getDeclaredField("mNavButtonView");
                fNavBtn.setAccessible(true);
                btnToolbarButton = (ImageButton) fNavBtn.get(toolbar);

            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            //if (btnToolbarButton != null)
            //YoYo.with(Techniques.FlipInX).duration(850).playOn(btnToolbarButton);
            YoYo.with(Techniques.FlipInX).duration(850).playOn(toolbar);
        }*/

//        Fragment fragment = fm.findFragmentByTag(LIST_FRAGMENT_TYPE);
//        if (fragment == null)
//            fragment = ArtistsListFragment.newInstance();

        if (savedInstanceState == null) {
            Log.d(TAG, "OnCreate savedInstanceState == null");

            updateListFilledHandler();
        }

        syncActionBarArrowState();
    }

    @Override
    protected void onStart() {
        super.onStart();

        showFragmentHandler();
    }

    private void updateListFilledHandler() {
        EventEnumBehavior.HANDLE_ARTISTS.subscribe()
                .observeOn(AndroidSchedulers.mainThread())
                .cast(Integer.class)
                .subscribe(this::updateListFilled);
    }

    private void updateListFilled(int state) {
        switch (state) {
            case ARTISTS_LIST_UNHANDLED:
                setLoadingState();
                break;
            case ARTISTS_LIST_ERROR:
                setErrorState();
                break;
            case ARTISTS_LIST_SUCCESS:
                setSuccessState();
        }
    }

    private void setLoadingState() {
        showLoadingFragment(ARTISTS_LIST_UNHANDLED);
    }

    private void setErrorState() {
        showLoadingFragment(ARTISTS_LIST_ERROR);
    }

    private void setSuccessState() {
        showListFragment();
    }

    public void showFragmentHandler() {
        compositeSubscription.add(
                EventEnumPublish.REPLACE_FRAGMENT.subscribe()
                        .observeOn(AndroidSchedulers.mainThread())
                        .cast(Artist.class)
                        .subscribe(artist -> {
                            showDetailsFragment(artist);
                            Log.d(TAG, "REPLACE_FRAGMENT msg got");
                        }, Throwable::printStackTrace));
    }

    public void showListFragment() {
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction()
                .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                .replace(R.id.main_frame, ArtistsListFragment.newInstance(), LIST_FRAGMENT_TYPE)
                .commit();
    }

    public void showLoadingFragment(int fragmentType) {
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.slide_out_right, android.R.anim.slide_in_left, android.R.anim.fade_out)
                .replace(R.id.main_frame, LoadingFragment.newInstance(fragmentType))
                .commit();
    }

    public void showDetailsFragment(Artist artist) {
        EventEnumBehavior.DETAILS_FRAGMENT_INFO.publish(artist);

        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction()
                .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right, android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                .addToBackStack(null)
                .replace(R.id.main_frame, ArtistsDetailFragment.newInstance())
                .commit();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (compositeSubscription != null && compositeSubscription.hasSubscriptions()) {
            compositeSubscription.clear();
        }
    }

    @Override
    protected void onDestroy() {
        getSupportFragmentManager().removeOnBackStackChangedListener(mOnBackStackChangedListener);
        if (compositeSubscription != null && !compositeSubscription.isUnsubscribed()) {
            compositeSubscription.unsubscribe();
        }
        ;

        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            onBackPressed();

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // turn on the Navigation Drawer image;
        // this is called in the LowerLevelFragments
        //Toast.makeText(this, "1111", Toast.LENGTH_LONG).show();
        syncActionBarArrowState();
    }

    private void syncActionBarArrowState() {
        int backStackEntryCount = getSupportFragmentManager().getBackStackEntryCount();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            /*if (btnToolbarButton != null)
                btnToolbarButton.setVisibility(backStackEntryCount != 0 ? View.VISIBLE : View.GONE);*/
            actionBar.setDisplayHomeAsUpEnabled(backStackEntryCount != 0);
        }
    }

    public void onFragmentInteraction(Uri uri) {
        //Toast.makeText(this, "2222", Toast.LENGTH_LONG).show();
    }
}
