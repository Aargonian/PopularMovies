package com.example.android.popularmovies;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Aaron Helton on 1/31/2016
 */
public class DetailFragment extends Fragment
{
    public DetailFragment() {}

    private Movie movie;
    private TextView title;
    private TextView rating;
    private TextView release;
    private TextView overview;
    private ImageView poster;
    private boolean viewCreated;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Intent intent = this.getActivity().getIntent();
        if(intent != null)
        {
            Integer id = intent.getIntExtra("MOVIE_ID", -1);
            if(id != -1)
            {
                setMovie(id);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_detail, container, false);
        title = (TextView)root.findViewById(R.id.movieTitle);
        rating = (TextView) root.findViewById(R.id.rating);
        overview = (TextView) root.findViewById(R.id.overview);
        release = (TextView) root.findViewById(R.id.releaseDate);
        poster = (ImageView) root.findViewById(R.id.posterImage);
        viewCreated = true;
        if(movie != null) {
            setMovieInfo(movie);
        }
        return root;
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
            title.setText(movie.getTitle());
            movie.applyPoster(poster);
            overview.setText(movie.getOverview());
            release.setText(movie.getReleaseDate());
            rating.setText(movie.getRating() + "/10");
        }
    }
}
