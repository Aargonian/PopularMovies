package com.example.android.popularmovies;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;

import com.nytegear.android.view.AsyncImage;
import com.nytegear.android.view.WebImageView;

import java.util.ArrayList;

/**
 * Created by Aaron Helton on 1/30/2016
 */
public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.ViewHolder>
{
    //Struct-like class for inner use
    public static class MovieReference {
        public final Integer id;
        public final String title;
        public final AsyncImage poster;

        public MovieReference(Integer id, String title, AsyncImage poster) {
            this.id = id;
            this.poster = poster;
            this.title = title;
        }
    }

    public interface MovieItemClickListener
    {
        void movieClicked(Integer movieID, String title);
    }

    private ArrayList<MovieReference> dataset;
    private MovieItemClickListener listener;
    private Context context;

    public MovieAdapter() {
        dataset = new ArrayList<>();
    }

    public class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, AsyncImage.AsyncImageListener {
        private String LOG_TAG = ViewHolder.class.getName();
        public ImageView imageView;
        public ViewHolder(ImageView v)
        {
            super(v);
            imageView = v;
            imageView.setOnClickListener(this);
        }

        @Override
        public void imageDownloaded(Bitmap bm)
        {
            Log.e(LOG_TAG, "IMAGE DOWNLOADED WOOT");
            imageView.setImageBitmap(bm);
        }

        @Override
        public void downloadFailed()
        {
            imageView.setImageBitmap(
                    BitmapFactory.decodeResource(context.getResources(), R.drawable.noimage));
        }

        @Override
        public void onClick(View v) {
            if(listener != null) {
                MovieReference reference = dataset.get(this.getLayoutPosition());
                listener.movieClicked(reference.id, reference.title);
            }
        }
    }

    public void setMovieItemClickedListener(MovieItemClickListener list)
    {
        this.listener = list;
    }

    @Override
    public MovieAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        WebImageView view = new WebImageView(parent.getContext());
        view.setAdjustViewBounds(true);
        view.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));
        view.setScaleType(ImageView.ScaleType.FIT_XY);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position)
    {
        MovieReference movie = dataset.get(position);
        if(movie != null) {
            movie.poster.setAsyncImageListener(holder);
            holder.imageView.setImageBitmap(movie.poster.getImage());
        }
    }

    public void addMovie(MovieReference reference)
    {
        dataset.add(reference);
        notifyItemInserted(dataset.size() - 1);
    }

    @Override
    public int getItemCount() {
        return dataset.size();
    }

    public final void emptyDataset() {
        int prevSize = dataset.size();
        dataset.clear();
        notifyItemRangeRemoved(0, prevSize);
    }
}
