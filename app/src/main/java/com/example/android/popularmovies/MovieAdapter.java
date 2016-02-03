package com.example.android.popularmovies;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;

import java.util.ArrayList;

/**
 * Created by Aaron Helton on 1/30/2016
 */
public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.ViewHolder>
{
    //Struct-like class for inner use
    public static class MovieReference {
        public final Integer id;
        public final Bitmap poster;
        public final String title;

        public MovieReference(Integer id, Bitmap poster, String title) {
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
            implements View.OnClickListener {
        public ImageView imageView;
        public ViewHolder(ImageView v)
        {
            super(v);
            imageView = v;
            imageView.setOnClickListener(this);
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
        ImageView view = new ImageView(parent.getContext());
        view.setAdjustViewBounds(true);
        view.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        view.setScaleType(ImageView.ScaleType.FIT_XY);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position)
    {
        MovieReference movie = dataset.get(position);
        if(movie != null) {
            holder.imageView.setImageBitmap(movie.poster);
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
}
