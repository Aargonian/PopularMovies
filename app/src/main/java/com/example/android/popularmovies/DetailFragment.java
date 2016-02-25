package com.example.android.popularmovies;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.popularmovies.data.MovieUtil;

import butterknife.Bind;
import butterknife.ButterKnife;
/**
 * Created by Aaron Helton on 1/31/2016
 */
public class DetailFragment extends Fragment
{
    private static final String LOG_TAG = DetailFragment.class.getSimpleName();
    public DetailFragment() {}

    @Bind(R.id.rating) TextView rating;
    @Bind(R.id.releaseDate) TextView release;
    @Bind(R.id.overview) TextView overview;
    @Bind(R.id.titleView) TextView title;
    @Bind(R.id.posterImage) ImageView poster;

    private MovieInfo movie;
    private boolean viewCreated;
    private boolean displayTitle = false;

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        Log.d(LOG_TAG, "ATTACHED TO ACTIVITY");
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "DETAIL FRAGMENT BEING CREATED");
        Intent intent = this.getActivity().getIntent();
        if(savedInstanceState != null)
        {
            movie = MovieInfo.buildMovie()
                    .withTitle(savedInstanceState.getString("MOVIE_TITLE"))
                    .withOverview(savedInstanceState.getString("MOVIE_OVERVIEW"))
                    .withPoster((Bitmap)savedInstanceState.getParcelable("MOVIE_POSTER"))
                    .withReleaseDate(savedInstanceState.getString("MOVIE_RELEASE"))
                    .withRuntime(savedInstanceState.getInt("MOVIE_RUNTIME"))
                    .withRating(savedInstanceState.getDouble("MOVIE_RATING"))
                    .withVoteCount(savedInstanceState.getInt("MOVIE_VOTES"))
                    .withGenres(savedInstanceState.getStringArray("MOVIE_GENRES")).build();
        } else  {
            Long id = intent.getLongExtra("MOVIE_ID", -1);
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
                             Bundle savedInstanceState)
    {
        View root = inflater.inflate(R.layout.fragment_detail, container, false);
        ButterKnife.bind(this, root);
        if(!displayTitle)
            title.setVisibility(View.GONE);
        viewCreated = true;
        if(movie != null) {
            setMovieInfo(movie);
        } else {
            movie = MovieInfo.buildMovie().withTitle("Pick a Movie!")
                    .withOverview("You can browse movies in the list and " +
                    "touch one to see its details here!")
                    .withPoster(
                            BitmapFactory.decodeResource(this.getResources(), R.drawable.noimage))
                    .build();
            setMovieInfo(movie);
        }
        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        Log.d(LOG_TAG, "ACTIVITY CREATED");
    }

    @Override
    public void onStart()
    {
        super.onStart();
        Log.d(LOG_TAG, "FRAGMENT STARTED");
    }

    @Override
    public void onResume()
    {
        super.onResume();
        Log.d(LOG_TAG, "FRAGMENT RESUMED");
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


    public void setMovie(Long id) {
        setMovieInfo(MovieUtil.getMovie(getActivity(), id));
    }

    @SuppressLint("SetTextI18n")
    private void setMovieInfo(MovieInfo movie)
    {
        this.movie = movie;
        if(viewCreated) {
            movie.applyPoster(poster);
            overview.setText(movie.getOverview());
            release.setText(movie.getReleaseDate());
            rating.setText(movie.getRating() + getString(R.string.out_of_ten));
            title.setText(movie.getTitle());
        }
    }
}
