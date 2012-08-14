ImageGrabber
============

The ImageGrabber library provides a utility to automatically download and cache images from the web. This library uses memory and disk caching efficiently and provide users with the option to force a network response from the web to query for updates even when the image is cached.

**Download:** [JAR library](https://github.com/herroWorld/ImageGrabber/downloads)

## Usage
When using this library, simply add the JAR to your project:

```
Right click on project --> Properties --> Java Build Path --> Libraries --> Add External JARs
```

### Example
```
ImageView imageView = ...
String imageUrl = ...

// Setting the image cache directory and cache size
ImageCacheParams cacheParams = new ImageCacheParams("thumbs");
cacheParams.memCacheSize = 1024 * 1024 * Utils.getMemoryClass(getApplicationContext()) / 3;

// Initializing an ImageGrabber object with 100 as the image width and height
ImageGrabber imageGrabber = new ImageGrabber(getApplicationContext(), 100);

// Setting a placeholder image when image is loading
imageGrabber.setLoadingImage(R.drawable.loading_image);

// Setting an image cache for the utility
ImageCache imageCache = new ImageCache(getApplicationContext(), cacheParams);
imageGrabber.setImageCache(mImageCache);

// Forcing a network response from server to check for updates even when image is cached
imageGrabber.setAlwaysFetchFromServer(true);

// Loading the image from any given URL
imageGrabber.loadImage(imageUrl, imageView);
```

## To Do
* Add functionality for local files as well

## License
* [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html)

## Acknowledgements
This library consists of code mostly from the Android Open Source Project.
