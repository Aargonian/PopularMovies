package com.nytegear.android.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.IOException;

/**
 * Created by Aaron Helton on 2/11/2016
 */
public class WebImageView extends ImageView
{
    private Context context;
    private Bitmap placeholder;
    private Bitmap image;

    public WebImageView(Context context)
    {
        super(context);
        this.context = context;
        this.placeholder = null;
        this.image = null;
    }

    public WebImageView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        this.context = context;
        this.placeholder = null;
        this.image = null;
    }

    public void setPlaceholderImage(Bitmap bitmap)
    {
        placeholder = bitmap;
        if(image == null) {
            this.setImageBitmap(placeholder);
        }
    }

    public void setImageUrl(String urlString)
    {
        PosterFetchTask task = new PosterFetchTask();
        task.execute(urlString);
    }

    private class PosterFetchTask extends AsyncTask<String, Void, Bitmap>
    {
        private final String LOG_TAG = PosterFetchTask.class.getSimpleName();

        @Override
        public Bitmap doInBackground(String... params)
        {
            if(params.length < 0 || params[0].isEmpty())
                return null;
            try {
                return Picasso.with(context).load(params[0]).get();
            } catch (IOException ex) {
                return placeholder;
            }
            //return NetworkUtil.getImage(params[0], context, placeholder);
        }

        @Override
        public void onPostExecute(Bitmap result)
        {
            image = result;
            if(image != null) {
                setImageBitmap(image);
            }
        }
    }
}
