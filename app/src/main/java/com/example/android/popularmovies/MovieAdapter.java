package com.example.android.popularmovies;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;

import java.util.ArrayList;

/**
 * Created by Aaron Helton on 1/30/16.
 */
public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.ViewHolder>
{
    private ArrayList<Movie> dataset;
    private MovieItemClickListener listener;

    public interface MovieItemClickListener
    {
        void movieClicked(Movie movie);
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
            listener.movieClicked(dataset.get(this.getLayoutPosition()));
        }
    }

    public MovieAdapter(MovieItemClickListener listener) {
        dataset = new ArrayList<>();
        this.listener = listener;
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
        Movie movie = dataset.get(position);
        if(movie != null)
            dataset.get(position).applyPoster(holder.imageView);
    }

    public void addMovie(Movie movie)
    {
        dataset.add(movie);
        notifyItemInserted(dataset.size()-1);
    }

    @Override
    public int getItemCount() {
        return dataset.size();
    }
}
