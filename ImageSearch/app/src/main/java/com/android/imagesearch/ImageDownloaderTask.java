package com.android.imagesearch;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;


import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;


public class ImageDownloaderTask   extends AsyncTask<String, Void, Bitmap> {

    private final WeakReference<ImageView> documentsImageViewReference;
    private LruCache<String, Bitmap> imageCache;

    public void addBitmapToCache(String key, Bitmap bitmap) {
        if (getBitmapFromCache(key) == null && bitmap !=null) {
            imageCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromCache(String key) {
        return imageCache.get(key);
    }

    public ImageDownloaderTask(ImageView imageView, LruCache<String,Bitmap> imageCache)
    {
        this.imageCache = imageCache;
        documentsImageViewReference = new WeakReference<ImageView>(imageView);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        ImageView imageView =documentsImageViewReference.get();
        Drawable placeholder = imageView.getContext().getResources().getDrawable(R.drawable.progress);
        imageView.setImageDrawable(placeholder);
        imageView.setScaleX(0.5F);
        imageView.setScaleY(0.5F);
    }

    @Override
    protected Bitmap doInBackground(String... params) {

        Bitmap image;
        if(getBitmapFromCache(params[0])==null)
        {
            Log.d("Cache","Downling from Server"+params[0]);
            image = downloadBitmap(params[0]);
            addBitmapToCache(params[0],image);
        }
        else
        {

            Log.d("Cache","Loading from Cache"+params[0]);
            image = getBitmapFromCache(params[0]);
        }



        return image;
    }

    private Bitmap downloadBitmap(String url) {
        // TODO Auto-generated method stub
        HttpURLConnection urlConnection = null;
        try {
            URL uri = new URL(url);
            urlConnection = (HttpURLConnection) uri.openConnection();

            InputStream inputStream = urlConnection.getInputStream();
            if (inputStream != null) {
                return BitmapFactory.decodeStream(inputStream);
            }
        } catch (Exception e) {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            Log.w("ImageDownloader", "Error downloading image from " + url);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        return null;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {


        if (documentsImageViewReference != null) {
            ImageView imageView =documentsImageViewReference.get();
            if (imageView != null) {
                if (bitmap != null) {

                    imageView.setImageBitmap(bitmap);
                } else {
                    Drawable placeholder = imageView.getContext().getResources().getDrawable(R.drawable.progress);
                    imageView.setImageDrawable(placeholder);
                }
                imageView.animate().scaleX(1.0F);
                imageView.animate().scaleY(1.0F);
            }
        }
    }

}

