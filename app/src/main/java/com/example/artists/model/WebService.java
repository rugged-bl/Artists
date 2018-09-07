package com.example.artists.model;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

import com.example.artists.connection.DownloadManager;
import com.example.artists.connection.DownloadRequest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Date;

import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

public class WebService extends Service {
    // Tag used for debugging/logging
    public static final String TAG = "WebService";

    public static final String URL_FILE_KEY = "UrlFile";
    private static String URL_FILE;
    Uri destinationUri;
    // CompositeSubscription used for managing subscriptions
    private CompositeSubscription compositeSubscription;

    @Override
    public void onCreate() {
        compositeSubscription = new CompositeSubscription();

        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        URL_FILE = intent.getExtras().getString(URL_FILE_KEY);
        File externalCacheDir = this.getExternalCacheDir();
        if (externalCacheDir != null)
            destinationUri = Uri.parse(externalCacheDir.toString() + "/" + URL_FILE.hashCode());

        File file = new File(destinationUri.toString());

        // check if cached file is not up to date
        Calendar date1 = Calendar.getInstance();
        date1.setTime(new Date((System.currentTimeMillis())));
        Calendar date2 = Calendar.getInstance();
        date2.setTime(new Date(file.lastModified()));
        date2.add(Calendar.HOUR, 12);

        // if its time to reload json, reload it,
        // if its not time, emit the information that its already loaded
        if (!file.exists() || file.length() < 240739 /*correct size of json*/ || date1.after(date2)) //size-костыль
            downloadFile();
        else
            EventEnumBehavior.DOWNLOAD_FINISHED.publish(null);

        // subscription which obtains information about download completed
        Subscription subscription = EventEnumBehavior.DOWNLOAD_FINISHED.subscribe()
                .cast(DownloadRequest.class)
                .subscribe(o1 -> {
                    if (o1 != null)
                        Log.d(TAG, o1.getError().getErrorCode() + " " + o1.getError().getErrorMessage());

                    // reads and emits the json file
                    String content = readFile(file);
                    EventEnumBehavior.PUBLISH_FILE.publish(content);

                    stopSelf();
                });
        compositeSubscription.add(subscription);

        return START_NOT_STICKY;
    }

    private void downloadFile() {
        DownloadRequest downloadRequest = new DownloadRequest(Uri.parse(URL_FILE))
                .setDestinationURI(destinationUri);
        DownloadManager downloadManager = new DownloadManager(downloadRequest);

        downloadManager.start();
        //DownloadManager emits DOWNLOAD_FINISHED of EventEnumBehavior when completed
    }

    private String readFile(File file) {
        String content = "";

        try {
            // открываем поток для чтения
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(
                            new FileInputStream(file)
                    ));
            String str = "";
            // читаем содержимое
            while ((str = br.readLine()) != null) {
                content += str;
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return content;
    }

    @Override
    public void onDestroy() {
        if (compositeSubscription != null && !compositeSubscription.isUnsubscribed()) {
            compositeSubscription.unsubscribe();
        }

        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
