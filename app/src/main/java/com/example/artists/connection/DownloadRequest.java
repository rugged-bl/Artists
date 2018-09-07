package com.example.artists.connection;

import android.net.Uri;

public class DownloadRequest {
    /**
     * The URI resource that this request is to download
     */
    private Uri uri;
    private int currentTimeout = 40 * 1000;
    private boolean deleteDestinationFileOnFailure = true;
    /**
     * The destination path on the device where the downloaded files needs to be put
     * It can be either External Directory ( SDcard ) or
     * internal app cache or files directory.
     * For using external SDCard access, application should have
     * this permission android.permission.WRITE_EXTERNAL_STORAGE declared.
     */
    private Uri destinationURI;
    private Error error;

    public DownloadRequest(Uri uri) {
        if (uri == null) {
            throw new NullPointerException();
        }

        String scheme = uri.getScheme();
        if (scheme == null || (!scheme.equals("http") && !scheme.equals("https"))) {
            throw new IllegalArgumentException("Can only download HTTP/HTTPS URIs: " + uri);
        }

        this.uri = uri;
    }

    public Uri getUri() {
        return uri;
    }

    public DownloadRequest setUri(Uri mUri) {
        this.uri = mUri;
        return this;
    }

    public Uri getDestinationURI() {
        return destinationURI;
    }

    public DownloadRequest setDestinationURI(Uri destinationURI) {
        this.destinationURI = destinationURI;
        return this;
    }

    public int getCurrentTimeout() {
        return currentTimeout;
    }

    public DownloadRequest setCurrentTimeout(int currentTimeout) {
        this.currentTimeout = currentTimeout;
        return this;
    }

    public boolean getDeleteDestinationFileOnFailure() {
        return deleteDestinationFileOnFailure;
    }

    /**
     * Set if destination file should be deleted on download failure.
     * Use is optional: default is to delete.
     */
    public DownloadRequest setDeleteDestinationFileOnFailure(boolean deleteOnFailure) {
        this.deleteDestinationFileOnFailure = deleteOnFailure;
        return this;
    }

    public Error getError() {
        return error;
    }

    public void setError(Error error) {
        this.error = error;
    }
}
