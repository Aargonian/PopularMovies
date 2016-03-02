package com.example.android.popularmovies;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.android.popularmovies.data.MovieContract;
import com.example.android.popularmovies.data.MovieUtil;

import butterknife.Bind;
import butterknife.ButterKnife;
/**
 * Created by Aaron Helton on 1/31/2016
 */
public class DetailFragment extends Fragment implements View.OnClickListener
{
    private static final String LOG_TAG = DetailFragment.class.getSimpleName();
    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Bind(R.id.rating) TextView rating;
    @Bind(R.id.releaseDate) TextView release;
    @Bind(R.id.runtime) TextView runtime;
    @Bind(R.id.overview) TextView overview;
    @Bind(R.id.titleView) TextView title;
    @Bind(R.id.posterImage) ImageView poster;
    @Bind(R.id.star_icon) ImageView favoriteIcon;
    @Bind(R.id.favoriteButton) View favoriteButton;
    @Bind(R.id.reviewHeader) TextView reviewHeader;
    @Bind(R.id.reviewList) LinearLayout reviews;
    @Bind(R.id.trailerList) LinearLayout trailerList;

    private ShareActionProvider shareActionProvider;
    private MovieInfo movie;
    private boolean viewCreated;
    private boolean displayTitle = false;

    @Override
    public void onAttach(Context context) {
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
        if(intent.getLongExtra("MOVIE_ID", -1) == -1)
        {
            displayTitle = true;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        inflater.inflate(R.menu.detail_fragment, menu);
        MenuItem menuItem = menu.findItem(R.id.action_share);
        shareActionProvider =
                (ShareActionProvider)MenuItemCompat.getActionProvider(menuItem);
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
        favoriteButton.setOnClickListener(this);
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


    private void setMovieInfo(MovieInfo movie)
    {
        this.movie = movie;
        if(viewCreated) {
            movie.applyPoster(poster);
            overview.setText(movie.getOverview());
            release.setText(movie.getReleaseDate());
            rating.setText(movie.getRating() + getString(R.string.out_of_ten));
            runtime.setText(movie.getRunTime() + getString(R.string.runtime_unit));
            title.setText(movie.getTitle());

            if(MovieUtil.isFavorite(getActivity(), movie.getId())) {
                favoriteIcon.setImageBitmap(BitmapFactory.decodeResource(getResources(),
                                                                         R.drawable.star_favorite));
            } else {
                favoriteIcon.setImageBitmap(BitmapFactory.decodeResource(getResources(),
                                                                    R.drawable.star_not_favorite));
            }

            trailerList.removeAllViews();
            if(movie.getTrailers() != null && movie.getTrailers().length > 0) {
                final String[] trailers = movie.getTrailers();
                for(int i = 0; i < trailers.length; i++) {
                    View view = getLayoutInflater(null).inflate(R.layout.trailer_layout, null);
                    TextView trailerText = (TextView)view.findViewById(R.id.trailerText);
                    trailerText.setText("Trailer " + (i+1));
                    view.setOnClickListener(new TrailerClickListener(trailers[i]));
                    trailerList.addView(view);
                }
                setShareMovieTrailerIntent(trailers[0]);
            }

            reviews.removeAllViews();
            if(movie.getReviews() != null && movie.getReviews().length > 0) {
                reviewHeader.setVisibility(View.VISIBLE);
                reviews.addView(reviewHeader); //Because it clears everytime we change movies
                for(int i = 0; i < movie.getReviews().length; i++) {
                    TextView view =
                            (TextView)getLayoutInflater(null).inflate(R.layout.review_layout, null);
                    view.setText(movie.getReviews()[i]);
                    reviews.addView(view);

                    View divider = new View(getActivity());
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT, 1);
                    int px = (int) TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics());
                    params.setMargins(0, px, 0, px);
                    divider.setLayoutParams(params);
                    reviews.addView(divider);
                }
            }
        }
    }

    private void setShareMovieTrailerIntent(String trailer) {
        if(shareActionProvider == null)
            return;
        Intent shareTrailerIntent = new Intent(Intent.ACTION_SEND);
        //noinspection deprecation
        shareTrailerIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareTrailerIntent.setType("text/plain");
        shareTrailerIntent.putExtra(Intent.EXTRA_TEXT, trailer);
        shareActionProvider.setShareIntent(shareTrailerIntent);
    }

    @Override
    public void onClick(View v) {
        if(v == favoriteButton) {
            if(movie.getId() > 0) {
                if(MovieUtil.isFavorite(getActivity(), movie.getId())) {
                    setFavorite(movie.getId(), false);
                } else {
                    setFavorite(movie.getId(), true);
                }
            }
        }
    }

    private void setFavorite(Long id, boolean favorite)
    {
        int favVal = favorite ? 1 : 0;
        ContentValues vals = new ContentValues();
        vals.put(MovieContract.MovieEntry.COLUMN_FAVORITE, favVal);
        getActivity().getContentResolver().update(MovieContract.MovieEntry.CONTENT_URI,
                                                  vals,
                                                  MovieContract.MovieEntry.COLUMN_TMDB_ID + " = ?",
                                                  new String[]{Long.toString(id)});
        Bitmap favIcon = BitmapFactory.decodeResource(getResources(),
                favorite ? R.drawable.star_favorite : R.drawable.star_not_favorite);
        favoriteIcon.setImageBitmap(favIcon);
    }

    private class TrailerClickListener implements View.OnClickListener {
        private final String trailer;

        public TrailerClickListener(String trailer) {
            this.trailer = trailer;
        }

        @Override
        public void onClick(View v) {
            Log.d(LOG_TAG, "SHOWING TRAILER: " + trailer);
            Intent trailerIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(trailer));
            startActivity(trailerIntent);
        }
    }
}
