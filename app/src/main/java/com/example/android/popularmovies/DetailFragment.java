package com.example.android.popularmovies;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.popularmovies.data.FileUtils;
import com.example.android.popularmovies.data.MovieContract.MovieEntry;

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

    private final String[] projection = {
            MovieEntry.COLUMN_TITLE,
            MovieEntry.COLUMN_DESC,
            MovieEntry.COLUMN_IMG_PATH,
            MovieEntry.COLUMN_RELEASE,
            MovieEntry.COLUMN_RUNTIME,
            MovieEntry.COLUMN_RATING,
            MovieEntry.COLUMN_VOTE_CNT,
            MovieEntry.COLUMN_GENRES
    };


    @SuppressWarnings("all") private final int TITLE = 0;
    @SuppressWarnings("all") private final int OVERVIEW = 1;
    @SuppressWarnings("all") private final int IMG_PATH = 2;
    @SuppressWarnings("all") private final int RELEASE = 3;
    @SuppressWarnings("all") private final int RUNTIME = 4;
    @SuppressWarnings("all") private final int RATING = 5;
    @SuppressWarnings("all") private final int VOTE_COUNT = 6;
    @SuppressWarnings("all") private final int GENRES = 7;

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


    public void setMovie(Long id) {
        Cursor cur = getActivity().getContentResolver().query(MovieEntry.buildMovieUriWithId(id),
                projection, null, null, null);
        if (cur == null || !cur.moveToFirst())
            return;
        movie = new Movie(
                cur.getString(TITLE),
                cur.getString(OVERVIEW),
                FileUtils.getImage(cur.getString(IMG_PATH)),
                cur.getString(RELEASE),
                cur.getInt(RUNTIME),
                cur.getDouble(RATING),
                cur.getInt(VOTE_COUNT),
                parseGenres(cur.getString(GENRES))
        );
        cur.close();
        setMovieInfo(movie);
    }

    private String[] parseGenres(String genres)
    {
        return genres.split("_");
    }

    private void setMovieInfo(Movie movie)
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
