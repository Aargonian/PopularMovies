package com.example.android.popularmovies;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;

import com.example.android.popularmovies.data.FileUtils;
import com.example.android.popularmovies.data.MovieContract;

/**
 * Created by Aaron Helton on 1/30/2016
 */
public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.ViewHolder>
{
    @SuppressWarnings("unused")
    private static final String LOG_TAG = MovieAdapter.class.getSimpleName();

    public interface MovieItemClickListener
    {
        void movieClicked(Long movieID);
    }

    private Cursor dataset;
    private MovieItemClickListener listener;

    public MovieAdapter() {
        dataset = null;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        @SuppressWarnings("unused")
        private final String LOG_TAG = ViewHolder.class.getName();

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
                if(dataset != null) {
                    dataset.moveToPosition(this.getLayoutPosition());
                    long id = dataset.getLong(
                            dataset.getColumnIndex(MovieContract.MovieEntry.COLUMN_TMDB_ID));
                    listener.movieClicked(id);
                }
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
        view.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));
        view.setScaleType(ImageView.ScaleType.FIT_XY);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position)
    {
        if(dataset != null) {
            dataset.moveToPosition(position);
            Bitmap poster = FileUtils.getImage(
                    dataset.getString(
                            dataset.getColumnIndex(MovieContract.MovieEntry.COLUMN_IMG_PATH)));
            if (poster != null) {
                holder.imageView.setImageBitmap(poster);
            }
        }
    }

    @Override
    public int getItemCount() {
        if(dataset != null) {
            return dataset.getCount();
        } else {
            return 0;
        }
    }

    public void setDataset(Cursor cursor)
    {
        this.dataset = cursor;
        this.notifyDataSetChanged();
    }
}
