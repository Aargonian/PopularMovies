package com.nytegear.android.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.nytegear.android.network.NetworkUtil;

/**
 * Created by Aaron Helton on 2/11/2016
 */
public class AsyncImage
{
    private Context context;
    private Bitmap placeholder;
    private Bitmap image;
    private AsyncImageListener listener;

    public interface AsyncImageListener {
        void imageDownloaded(Bitmap bm);
        void downloadFailed();
    }

    public AsyncImage(Context context)
    {
        this.context = context;
    }

    public AsyncImage(Context context, Bitmap placeholder)
    {
        this.context = context;
        this.placeholder = placeholder;
    }

    public void setAsyncImageListener(AsyncImageListener listener)
    {
        this.listener = listener;
    }

    public void setImageURL(String url)
    {
        PosterFetchTask task = new PosterFetchTask();
        task.execute(url);
    }

    public Bitmap getImage()
    {
        return image == null ? placeholder : image;
    }

    public Bitmap getPlaceholder()
    {
        return placeholder;
    }

    private class PosterFetchTask extends AsyncTask<String, Void, Bitmap>
    {
        private final String LOG_TAG = PosterFetchTask.class.getSimpleName();

        @Override
        public Bitmap doInBackground(String... params)
        {
            if(params.length < 0 || params[0].isEmpty())
                return null;
            return NetworkUtil.getImage(params[0], context, placeholder);
        }

        @Override
        public void onPostExecute(Bitmap result)
        {
            image = result;
            if(image != null && image != placeholder) {
                if(listener != null) {
                    listener.imageDownloaded(result);
                }
            } else {
                if(listener != null) {
                    listener.downloadFailed();
                }
            }
        }
    }
}
