
package com.herroworld.imagegrabber;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.impl.cookie.DateUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

/**
 * A simple subclass of {@link ImageResizer} that fetches and resizes images
 * fetched from a URL.
 */
public class ImageGrabber extends ImageResizer {
    private static final String TAG = "ImageGrabber";
    protected int mCacheSize = 10 * 1024 * 1024; // 10MB
    protected String mCacheDir = "http";

    /**
     * Initialize providing a target image width and height for the processing
     * images.
     * 
     * @param context
     * @param imageWidth
     * @param imageHeight
     */
    public ImageGrabber(Context context, int imageWidth, int imageHeight) {
        super(context, imageWidth, imageHeight);
        init(context);
    }

    /**
     * Initialize providing a single target image size (used for both width and
     * height);
     * 
     * @param context
     * @param imageSize
     */
    public ImageGrabber(Context context, int imageSize) {
        super(context, imageSize);
        init(context);
    }

    private void init(Context context) {
        checkConnection(context);
    }

    /**
     * Simple network connection check.
     * 
     * @param context
     */
    private void checkConnection(Context context) {
        final ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnectedOrConnecting()) {
            Toast.makeText(context, "No network connection found.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "checkConnection - no connection found");
        }
    }

    /**
     * Setter for http cache size, default is 10MB.
     * 
     * @param cacheSize In bytes.
     */
    public void setHttpCacheSize(int cacheSize) {
        if (cacheSize > 0) {
            mCacheSize = cacheSize;
        } else {
            Log.e(TAG, "cache size cannot be zero or negative!");
        }
    }

    /**
     * Setter for http cache directory, default is http.
     * 
     * @param dirName
     */
    public void setHttpCacheDir(String dirName) {
        if ((dirName != null) && (dirName.length() != 0)) {
            mCacheDir = dirName;
        } else {
            Log.e(TAG, "cache directory name cannot be null or empty!");
        }
    }

    /**
     * The main process method, which will be called by the ImageWorker in the
     * AsyncTask background thread.
     * 
     * @param data The data to load the bitmap, in this case, a regular http URL
     * @param forceFetch Whether or not to force a network response even if the
     *            image is cached
     * @return The downloaded and resized bitmap
     */
    private Bitmap processBitmap(String data, boolean forceFetch) {
        if (Utils.DEBUG) {
            Log.d(TAG, "processBitmap - " + data);
        }

        // Download a bitmap, write it to a file
        final File f = downloadBitmap(mContext, data, forceFetch);

        if (f != null) {
            // Return a sampled down version
            return decodeSampledBitmapFromFile(f.toString(), mImageWidth, mImageHeight);
        }

        return null;
    }

    @Override
    protected Bitmap processBitmap(Object data, boolean forceFetch) {
        return processBitmap(String.valueOf(data), forceFetch);
    }

    /**
     * Download a bitmap from a URL, write it to a disk and return the File
     * pointer. This implementation uses a simple disk cache.
     * 
     * @param context The context to use
     * @param urlString The URL to fetch
     * @param forceFetch Whether or not to force a network response even if the
     *            image is cached
     * @return A File pointing to the fetched bitmap
     */
    public File downloadBitmap(Context context, String urlString, boolean forceFetch) {
        final File cacheDir = DiskLruCache.getDiskCacheDir(context, mCacheDir);

        final DiskLruCache cache =
                DiskLruCache.openCache(context, cacheDir, mCacheSize);

        if (cache == null) {
            return null;
        }

        final File cacheFile = new File(cache.createFilePath(urlString));

        if (cache.containsKey(urlString) && !forceFetch) {
            if (Utils.DEBUG) {
                Log.d(TAG, "downloadBitmap - found in http cache - " + urlString);
            }
            return cacheFile;
        }

        if (Utils.DEBUG) {
            Log.d(TAG, "downloadBitmap - downloading - " + urlString);
        }

        Utils.disableConnectionReuseIfNecessary();
        HttpURLConnection urlConnection = null;
        BufferedOutputStream out = null;

        try {
            final URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();

            if (isModified(cacheFile.lastModified(), urlConnection)) {
                final InputStream in =
                        new BufferedInputStream(urlConnection.getInputStream(),
                                Utils.IO_BUFFER_SIZE);
                out = new BufferedOutputStream(new FileOutputStream(cacheFile),
                        Utils.IO_BUFFER_SIZE);

                int b;
                while ((b = in.read()) != -1) {
                    out.write(b);
                }
            }
            return cacheFile;

        } catch (final IOException e) {
            Log.e(TAG, "Error in downloadBitmap - " + e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (out != null) {
                try {
                    out.close();
                } catch (final IOException e) {
                    Log.e(TAG, "Error in downloadBitmap - " + e);
                }
            }
        }

        return null;
    }

    /**
     * Checking for a server status code 304 response to determine if the image
     * has been modified since the last time the file was written
     * 
     * @param lastModified File last modified time
     * @param urlConnection
     * @return If file on server has been modified
     * @throws IOException
     */
    public boolean isModified(long lastModified, HttpURLConnection urlConnection)
            throws IOException {
        String date = DateUtils.formatDate(new Date(lastModified),
                "EEE, dd MMM yyy HH:mm:ss 'GMT'");
        urlConnection.setRequestProperty("If-Modified-Since", date);

        return (urlConnection.getResponseCode() != HttpURLConnection.HTTP_NOT_MODIFIED);
    }
}
