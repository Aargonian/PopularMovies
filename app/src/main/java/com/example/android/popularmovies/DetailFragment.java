package com.example.android.popularmovies;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Aaron Helton on 1/31/2016
 */
public class DetailFragment extends Fragment
{
    public DetailFragment() {}

    private Movie movie;
    @Bind(R.id.rating) TextView rating;
    @Bind(R.id.releaseDate) TextView release;
    @Bind(R.id.overview) TextView overview;
    @Bind(R.id.titleView) TextView title;
    @Bind(R.id.posterImage) ImageView poster;
    private boolean viewCreated;
    private boolean displayTitle = false;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Intent intent = this.getActivity().getIntent();
        if(savedInstanceState != null)
        {
            movie = new Movie(
                    savedInstanceState.getString("MOVIE_TITLE"),
                    savedInstanceState.getString("MOVIE_OVERVIEW"),
                    (Bitmap)savedInstanceState.getParcelable("MOVIE_POSTER"),
                    savedInstanceState.getString("MOVIE_RELEASE"),
                    savedInstanceState.getInt("MOVIE_RUNTIME"),
                    savedInstanceState.getDouble("MOVIE_RATING"),
                    savedInstanceState.getInt("MOVIE_VOTES"),
                    savedInstanceState.getStringArray("MOVIE_GENRES")
            );
        } else  {
            Integer id = intent.getIntExtra("MOVIE_ID", -1);
            if(id != -1)
            {
                setMovie(id);
            }
        }

        //Check for MovieID here because, if it returns -1, the fragment was not called in a single
        //activity, and is likely being displayed along another fragment
        if(intent.getIntExtra("MOVIE_ID", -1) == -1)
        {
            displayTitle = true;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_detail, container, false);
        ButterKnife.bind(this, root);
        if(!displayTitle)
            title.setVisibility(View.GONE);
        viewCreated = true;
        if(movie != null) {
            setMovieInfo(movie);
        } else {
            movie = new Movie(
                    "Pick a Movie!",
                    "You can browse movies in the list and touch one to see its details here!",
                    BitmapFactory.decodeResource(this.getResources(), R.drawable.noimage),
                    "",
                    0,
                    10.0,
                    0,
                    new String[]{"None"}
            );
            setMovieInfo(movie);
        }
        return root;
    }

    @Override
    public void onSaveInstanceState(Bundle bundle)
    {
        super.onSaveInstanceState(bundle);
        if(movie != null) {
            bundle.putString("MOVIE_TITLE", movie.getTitle());
            bundle.putString("MOVIE_OVERVIEW", movie.getOverview());
            bundle.putString("MOVIE_RELEASE", movie.getReleaseDate());
            bundle.putDouble("MOVIE_RATING", movie.getRating());
            bundle.putInt("MOVIE_RUNTIME", movie.getRunTime());
            bundle.putParcelable("MOVIE_POSTER", movie.getPoster());
            bundle.putInt("MOVIE_VOTES", movie.getVoteCount());
            bundle.putStringArray("MOVIE_GENRES", movie.getGenres());
        }
    }


    public void setMovie(Integer id)
    {
        new AsyncTask<Integer, Void, Movie>() {
            @Override
            public Movie doInBackground(Integer... params)
            {
                if(params.length < 1)
                    return null;
                return Movie.getMovie(params[0], DetailFragment.this.getContext());
            }

            @Override
            public void onPostExecute(Movie movie)
            {
                DetailFragment.this.setMovieInfo(movie);
            }
        }.execute(id);
    }

    private void setMovieInfo(Movie movie)
    {
        this.movie = movie;
        if(viewCreated) {
            movie.applyPoster(poster);
            overview.setText(movie.getOverview());
            release.setText(movie.getReleaseDate());
            rating.setText(movie.getRating() + "/10");
            title.setText(movie.getTitle());
        }
    }
}
