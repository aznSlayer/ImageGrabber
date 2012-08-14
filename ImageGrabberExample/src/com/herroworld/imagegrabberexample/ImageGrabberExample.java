
package com.herroworld.imagegrabberexample;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.widget.ImageView;

import com.herroworld.imagegrabber.ImageCache;
import com.herroworld.imagegrabber.ImageCache.ImageCacheParams;
import com.herroworld.imagegrabber.ImageGrabber;
import com.herroworld.imagegrabber.Utils;

public class ImageGrabberExample extends Activity {
    private static final String IMAGE_CACHE_DIR = "thumbs";
    private ImageCache mImageCache;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setting the image cache directory and cache size
        ImageCacheParams cacheParams = new ImageCacheParams(IMAGE_CACHE_DIR);
        cacheParams.memCacheSize = 1024 * 1024 * Utils.getMemoryClass(getApplicationContext()) / 3;

        ImageView image = (ImageView) findViewById(R.id.imageView);
        ImageView image2 = (ImageView) findViewById(R.id.imageView2);

        // Initializing an ImageGrabber object with 100 as the image width and
        // height
        ImageGrabber imageGrabber = new ImageGrabber(getApplicationContext(), 100);

        // Setting a placeholder image when image is loading
        imageGrabber.setLoadingImage(R.drawable.empty_photo);

        // Setting an image cache for the utility
        mImageCache = new ImageCache(getApplicationContext(),
                cacheParams);
        imageGrabber.setImageCache(mImageCache);

        // Forcing a network response from server to check for updates even when
        // image is cached
        imageGrabber.setAlwaysFetchFromServer(true);

        // Loading the image from any given URL
        imageGrabber
                .loadImage(
                        "https://lh6.googleusercontent.com/-jZgveEqb6pg/T3R4kXScycI/AAAAAAAAAE0/xQ7CvpfXDzc/s1024/sample_image_01.jpg",
                        image);
        imageGrabber
                .loadImage(
                        "https://lh4.googleusercontent.com/-K2FMuOozxU0/T3R4lRAiBTI/AAAAAAAAAE8/a3Eh9JvnnzI/s1024/sample_image_02.jpg",
                        image2);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
}
