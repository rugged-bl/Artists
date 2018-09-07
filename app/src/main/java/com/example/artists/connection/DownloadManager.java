package com.example.artists.connection;

import android.os.Process;
import android.util.Log;

import com.example.artists.model.EventEnumBehavior;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;

import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;
import static java.net.HttpURLConnection.HTTP_MOVED_PERM;
import static java.net.HttpURLConnection.HTTP_MOVED_TEMP;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_PARTIAL;
import static java.net.HttpURLConnection.HTTP_SEE_OTHER;
import static java.net.HttpURLConnection.HTTP_UNAVAILABLE;

public class DownloadManager extends Thread {
    public final static int ERROR_DOWNLOAD_SIZE_UNKNOWN = 1006;
    /**
     * Error code when passed URI is malformed.
     */
    public final static int ERROR_MALFORMED_URI = 1007;
    /**
     * Error code when download is cancelled.
     */
    public final static int ERROR_DOWNLOAD_CANCELLED = 1008;
    /**
     * Tag used for debugging/logging
     */
    public static final String TAG = "DownloadManager";
    /**
     * The buffer size used to stream the data
     */
    public final int BUFFER_SIZE = 4096;
    /**
     * The maximum number of redirects.
     */
    public final int MAX_REDIRECTS = 5; // can't be more than 7.
    private final int HTTP_REQUESTED_RANGE_NOT_SATISFIABLE = 416;
    private final int HTTP_TEMP_REDIRECT = 307;
    boolean shouldAllowRedirects = true;
    /**
     * Current Download request that this dispatcher is working
     */
    private DownloadRequest request;
    /**
     * How many times redirects happened during a download request.
     */
    private int redirectionCount = 0;
    private long contentLength;

    /**
     * Constructor take the dependency (DownloadRequest queue) that all the Dispatcher needs
     */
    public DownloadManager(DownloadRequest request) {
        this.request = request;
    }

    @Override
    public void run() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

        Log.v(TAG, "Download initiated");
        executeDownload(request.getUri().toString());
    }

    private void executeDownload(String downloadUrl) {
        URL url;
        try {
            url = new URL(downloadUrl);
        } catch (MalformedURLException e) {
            updateDownloadFailed(ERROR_MALFORMED_URI, "MalformedURLException: URI passed is malformed.");
            return;
        }

        HttpURLConnection conn = null;

        try {
            conn = (HttpURLConnection) url.openConnection();
            conn.setInstanceFollowRedirects(false);
            conn.setConnectTimeout(request.getCurrentTimeout());
            conn.setReadTimeout(request.getCurrentTimeout());

            // urlConnection is trying to connect to destination.
            final int responseCode = conn.getResponseCode();

            Log.v(TAG, "Response code obtained for download"
                    + " : httpResponse Code "
                    + responseCode);

            switch (responseCode) {
                case HTTP_PARTIAL:
                case HTTP_OK:
                    shouldAllowRedirects = false;
                    if (readResponseHeaders(conn) == 1) {
                        transferData(conn);
                    } else {
                        updateDownloadFailed(ERROR_DOWNLOAD_SIZE_UNKNOWN, "Transfer-Encoding not found as well as can't know size of download, giving up");
                    }
                    return;
                case HTTP_MOVED_PERM:
                case HTTP_MOVED_TEMP:
                case HTTP_SEE_OTHER:
                case HTTP_TEMP_REDIRECT:
                    // Take redirect url and call executeDownload recursively until
                    // MAX_REDIRECT is reached.
                    while (redirectionCount++ < MAX_REDIRECTS && shouldAllowRedirects) {
                        Log.v(TAG, "Redirect for download");
                        final String location = conn.getHeaderField("Location");
                        executeDownload(location);
                    }

                    if (redirectionCount > MAX_REDIRECTS) {
                        updateDownloadFailed(android.app.DownloadManager.ERROR_TOO_MANY_REDIRECTS, "Too many redirects, giving up");
                        return;
                    }
                    break;
                case HTTP_REQUESTED_RANGE_NOT_SATISFIABLE:
                    updateDownloadFailed(HTTP_REQUESTED_RANGE_NOT_SATISFIABLE, conn.getResponseMessage());
                    break;
                case HTTP_UNAVAILABLE:
                    updateDownloadFailed(HTTP_UNAVAILABLE, conn.getResponseMessage());
                    break;
                case HTTP_INTERNAL_ERROR:
                    updateDownloadFailed(HTTP_INTERNAL_ERROR, conn.getResponseMessage());
                    break;
                default:
                    updateDownloadFailed(android.app.DownloadManager.ERROR_UNHANDLED_HTTP_CODE, "Unhandled HTTP response:" + responseCode + " message:" + conn.getResponseMessage());
                    break;
            }
        } catch (SocketTimeoutException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            updateDownloadFailed(android.app.DownloadManager.ERROR_HTTP_DATA_ERROR, "Trouble with low-level sockets");
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private void transferData(HttpURLConnection conn) {
        InputStream in = null;
        OutputStream out = null;
        FileDescriptor outFd = null;
        cleanupDestination();
        try {
            try {
                in = conn.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            File destinationFile = new File(request.getDestinationURI().getPath());

            boolean errorCreatingDestinationFile = false;
            // Create destination file if it doesn't exists
            if (!destinationFile.exists()) {
                try {
                    if (!destinationFile.createNewFile()) {
                        errorCreatingDestinationFile = true;
                        updateDownloadFailed(android.app.DownloadManager.ERROR_FILE_ERROR,
                                "Error in creating destination file");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    errorCreatingDestinationFile = true;
                    updateDownloadFailed(android.app.DownloadManager.ERROR_FILE_ERROR,
                            "Error in creating destination file");
                }
            }

            // If Destination file couldn't be created. Abort the data transfer.
            if (!errorCreatingDestinationFile) {
                try {
                    out = new FileOutputStream(destinationFile, true);
                    outFd = ((FileOutputStream) out).getFD();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (in == null) {
                    updateDownloadFailed(android.app.DownloadManager.ERROR_FILE_ERROR,
                            "Error in creating input stream");
                } else if (out == null) {
                    updateDownloadFailed(android.app.DownloadManager.ERROR_FILE_ERROR,
                            "Error in writing download contents to the destination file");
                } else {
                    // Start streaming data
                    transferData(in, out);
                }
            }

        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                if (out != null) {
                    out.flush();
                }
                if (outFd != null) {
                    outFd.sync();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (out != null) out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void transferData(InputStream in, OutputStream out) {
        final byte data[] = new byte[BUFFER_SIZE];

        Log.v(TAG, "Content Length: " + contentLength + " for Download");
        while (true) {
            int bytesRead = readFromResponse(data, in);

            if (bytesRead == -1) { // success, end of stream already reached
                updateDownloadComplete();
                return;
            } else if (bytesRead == Integer.MIN_VALUE) {
                return;
            }

            writeDataToDestination(data, bytesRead, out);
        }
    }

    private int readFromResponse(byte[] data, InputStream entityStream) {
        try {
            return entityStream.read(data);
        } catch (IOException e) {
            if ("unexpected end of stream".equals(e.getMessage())) {
                return -1;
            }
            updateDownloadFailed(android.app.DownloadManager.ERROR_HTTP_DATA_ERROR, "IOException: Failed reading response");
            return Integer.MIN_VALUE;
        }
    }

    private void writeDataToDestination(byte[] data, int bytesRead, OutputStream out) {
        while (true) {
            try {
                out.write(data, 0, bytesRead);
                return;
            } catch (Exception e) {
                updateDownloadFailed(android.app.DownloadManager.ERROR_FILE_ERROR, "IOException when writing download contents to the destination file");
            }
        }
    }

    private int readResponseHeaders(HttpURLConnection conn) {
        final String transferEncoding = conn.getHeaderField("Transfer-Encoding");
        contentLength = -1;

        if (transferEncoding == null) {
            contentLength = getHeaderFieldLong(conn, "Content-Length", -1);
        } else {
            Log.v(TAG, "Ignoring Content-Length since Transfer-Encoding is also defined for Download");
        }

        if (contentLength != -1) {
            return 1;
        } else if (transferEncoding == null || !transferEncoding.equalsIgnoreCase("chunked")) {
            return -1;
        } else {
            return 1;
        }
    }

    public long getHeaderFieldLong(URLConnection conn, String field, long defaultValue) {
        try {
            return Long.parseLong(conn.getHeaderField(field));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Called just before the thread finishes, regardless of status, to take any necessary action on
     * the downloaded file.
     */
    private void cleanupDestination() {
        Log.d(TAG, "cleanupDestination() deleting " + request.getDestinationURI().getPath());
        File destinationFile = new File(request.getDestinationURI().getPath());
        if (destinationFile.exists()) {
            destinationFile.delete();
        }
    }

    public void updateDownloadComplete() {
        request.setError(new Error(0, "No error"));
        EventEnumBehavior.DOWNLOAD_FINISHED.publish(request);
        //mDelivery.postDownloadComplete(request);
    }

    public void updateDownloadFailed(int errorCode, String errorMsg) {
        shouldAllowRedirects = false;

        if (request.getDeleteDestinationFileOnFailure()) {
            cleanupDestination();
        }
        request.setError(new Error(errorCode, errorMsg));
        EventEnumBehavior.DOWNLOAD_FINISHED.publish(request);
        //mDelivery.postDownloadFailed(request, errorCode, errorMsg);
    }
}
