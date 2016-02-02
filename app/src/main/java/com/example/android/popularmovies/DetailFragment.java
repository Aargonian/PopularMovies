package com.example.android.popularmovies;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by mint on 1/31/16.
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_detail, container, false);
        title = (TextView)root.findViewById(R.id.movieTitle);
        rating = (TextView) root.findViewById(R.id.rating);
        overview = (TextView) root.findViewById(R.id.overview);
        release = (TextView) root.findViewById(R.id.releaseDate);
        poster = (ImageView) root.findViewById(R.id.posterImage);

        if(movie != null) {
            title.setText(movie.getTitle());
            movie.applyPoster(poster);
            overview.setText(movie.getOverview());
            release.setText(movie.getReleaseDate());
            rating.setText(movie.getRating() + "/10");
        }

        return root;
    }

    public final void receiveMovie(Movie movie)
    {
        this.movie = movie;
        if(this.isVisible() && movie != null)
        {
            title.setText(movie.getTitle());
            movie.applyPoster(poster);
            overview.setText(movie.getOverview());
            release.setText(movie.getReleaseDate());
            rating.setText(movie.getRating() + "/10");
        }
    }
}
