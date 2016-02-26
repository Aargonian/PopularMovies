package com.example.android.popularmovies.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.android.popularmovies.BuildConfig;
import com.example.android.popularmovies.MovieInfo;
import com.example.android.popularmovies.R;
import com.nytegear.android.network.NetworkUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by Aaron Helton on 2/24/2016
 */
public final class MovieUtil
{
    private static final String LOG_TAG = MovieUtil.class.getSimpleName();

    public static final String API_KEY = BuildConfig.TMDBKey;
    public static final String TMDB_URL_BASE = "http://api.themoviedb.org/3";
    public static final String TMDB_POSTER_URL = "http://image.tmdb.org/t/p/w185";

    public static final String[] movieProjection = {
            MovieContract.MovieEntry.COLUMN_TMDB_ID,
            MovieContract.MovieEntry.COLUMN_TITLE,
            MovieContract.MovieEntry.COLUMN_DESC,
            MovieContract.MovieEntry.COLUMN_IMG_PATH,
            MovieContract.MovieEntry.COLUMN_RELEASE,
            MovieContract.MovieEntry.COLUMN_RUNTIME,
            MovieContract.MovieEntry.COLUMN_RATING,
            MovieContract.MovieEntry.COLUMN_POPULARITY,
            MovieContract.MovieEntry.COLUMN_VOTE_CNT,
            MovieContract.MovieEntry.COLUMN_GENRES,
            MovieContract.MovieEntry.COLUMN_FAVORITE
    };

    @SuppressWarnings("all") public static final int TMDB_ID = 0;
    @SuppressWarnings("all") public static final int TITLE = 1;
    @SuppressWarnings("all") public static final int OVERVIEW = 2;
    @SuppressWarnings("all") public static final int IMG_PATH = 3;
    @SuppressWarnings("all") public static final int RELEASE = 4;
    @SuppressWarnings("all") public static final int RUNTIME = 5;
    @SuppressWarnings("all") public static final int RATING = 6;
    @SuppressWarnings("all") public static final int POPULARITY = 7;
    @SuppressWarnings("all") public static final int VOTE_COUNT = 8;
    @SuppressWarnings("all") public static final int GENRES = 9;
    @SuppressWarnings("all") public static final int FAVORITE = 10;

    public static MovieInfo getMovie(@NonNull Context context, Long movieID)
    {
        Cursor cursor = context.getContentResolver().query(
                MovieContract.MovieEntry.CONTENT_URI,
                movieProjection,
                MovieContract.MovieEntry.COLUMN_TMDB_ID + " = ?",
                new String[]{Long.toString(movieID)},
                null
        );
        if(cursor == null) {
            Log.w(LOG_TAG, "Cursor is null!");
            return null;
        } else if (!cursor.moveToFirst()) {
            Log.w(LOG_TAG, "No movie retrieved! TMDB ID: " + movieID);
            cursor.close();
            //Check if we are on the UI Thread. If not, check network availability.
            //attempt to retrieve the image from the net!
            if(Looper.getMainLooper().getThread() != Thread.currentThread()
                    && NetworkUtil.isInternetAvailable(context)) {
                return getMovieFromNet(context, movieID);
            } else {
                Log.w(LOG_TAG, "Unable to retrieve the movie from network! Run off the UI Thread!");
            }
            return null;
        } else {
            MovieInfo movie =  MovieInfo.buildMovie().withId(movieID)
                    .withTitle(cursor.getString(TITLE))
                    .withOverview(cursor.getString(OVERVIEW))
                    .withPoster(FileUtils.getImage(cursor.getString(IMG_PATH)))
                    .withPosterPath(cursor.getString(IMG_PATH))
                    .withReleaseDate(cursor.getString(RELEASE))
                    .withRuntime(cursor.getInt(RUNTIME))
                    .withPopularity(cursor.getDouble(POPULARITY))
                    .withRating(cursor.getDouble(RATING))
                    .withVoteCount(cursor.getInt(VOTE_COUNT))
                    .withGenres(parseGenres(cursor.getString(GENRES))).build();
            cursor.close();
            return movie;
        }
    }

    public static boolean isFavorite(@NonNull Context context, Long movieID)
    {
        Cursor cursor = context.getContentResolver().query(
                MovieContract.MovieEntry.CONTENT_URI,
                new String[]{MovieContract.MovieEntry.COLUMN_FAVORITE},
                MovieContract.MovieEntry.COLUMN_TMDB_ID + " = ?",
                new String[]{Long.toString(movieID)},
                null
        );
        if(cursor == null) {
            Log.w(LOG_TAG, "Cursor was null!");
            return false;
        } else if(!cursor.moveToFirst()) {
            Log.w(LOG_TAG, "Unable to retrieve movie: " + movieID);
            cursor.close();
            return false;
        } else {
            boolean fave =  (cursor.getInt(0) == 1);
            cursor.close();
            return fave;
        }

    }

    /**
     * Forces grabbing the movie from off the network rather than from the local database. This
     * method updates the local database with the new movie info, then returns the resuling
     * MovieInfo object. This method should not be used from the UI Thread and will throw a
     * NetworkOnMainThreadException if that is the case.
     * @param context the context
     * @param id the TMDB id of the movie to get
     * @return the MovieInfo for the specified movie
     */
    public static MovieInfo getMovieFromNet(Context context, Long id)
    {
        String url = TMDB_URL_BASE+"/movie/"+id +"?api_key="+API_KEY;
        String movieJSON = NetworkUtil.getURL(url);
        if(movieJSON == null || movieJSON.isEmpty()) {
            Log.w(LOG_TAG, "Unable to fetch movie from network! TMDB ID: " + id);
            return null;
        }
        try
        {
            JSONObject movieObject = new JSONObject(movieJSON);
            JSONArray genreList = movieObject.getJSONArray("genres");
            String[] genres = new String[genreList.length()];
            for(int i = 0; i < genreList.length(); i++)
                genres[i] = genreList.getJSONObject(i).getString("name");
            String title = movieObject.getString("original_title");
            String overview = movieObject.getString("overview");
            String posterPath = movieObject.getString("poster_path");
            String releaseDate = movieObject.getString("release_date");

            //Individual Tries for Malformed Movies
            int runTime = 0;
            int votes = 0;
            double vote = 0;
            double popularity = 0;
            try {
                runTime = movieObject.getInt("runtime");
            } catch (JSONException ex) { Log.e(LOG_TAG, "BROKEN JSON: " + ex.getMessage(), ex); }
            try {
                votes = movieObject.getInt("vote_count");
            } catch (JSONException ex) { Log.e(LOG_TAG, "BROKEN JSON: " + ex.getMessage(), ex); }
            try {
                vote = movieObject.getDouble("vote_average");
            } catch (JSONException ex) { Log.e(LOG_TAG, "BROKEN JSON: " + ex.getMessage(), ex); }
            try {
                popularity = movieObject.getDouble("popularity");
            } catch (JSONException ex) { Log.e(LOG_TAG, "BROKEN JSON: " + ex.getMessage(), ex); }

            Bitmap poster = null;
            String imgPath = null;
            try {
                poster = getPoster(context, posterPath);
                imgPath = FileUtils.storeImage(context, poster, id + ".png");
            } catch (IOException ex) {
                Log.e(LOG_TAG, "Unable To Get MovieInfo Poster: " + ex.getMessage(), ex);
            }

            MovieInfo info = MovieInfo.buildMovie().withId(id)
                                         .withTitle(title)
                                         .withGenres(genres)
                                         .withPoster(poster)
                                         .withOverview(overview)
                                         .withReleaseDate((releaseDate))
                                         .withRuntime(runTime)
                                         .withRating(vote)
                                         .withVoteCount(votes)
                                         .withPopularity(popularity)
                                         .withPosterPath(imgPath).build();
            upsertMovie(context, info); //For caching purposes
            return info;
        } catch (JSONException ex) {
            Log.e(LOG_TAG, "ERROR PARSING MOVIE: " + ex.getMessage(), ex);
            Log.e(LOG_TAG, "JSON TEXT:\n"+movieJSON);
            return null;
        }
    }

    private static final Character GENRE_DELIMITER = '_';

    private static String[] parseGenres(String genres)
    {
        return genres.split(Character.toString(GENRE_DELIMITER));
    }

    private static String encodeGenres(String[] genres)
    {
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < genres.length; i++)
        {
            builder.append(genres[i]);
            if(i != genres.length-1)
                builder.append(GENRE_DELIMITER);
        }
        return builder.toString();
    }

    private static Bitmap getPoster(Context context, String posterPath) throws IOException
    {
        if(context == null || posterPath == null || posterPath.isEmpty())
            return null;
        return NetworkUtil.getImage(TMDB_POSTER_URL + posterPath, context,
                BitmapFactory.decodeResource(context.getResources(), R.drawable.loading));
    }

    private static void upsertMovie(@NonNull Context context, @NonNull MovieInfo movie)
    {
        Cursor cursor = context.getContentResolver().query(MovieContract.MovieEntry.CONTENT_URI,
                null, MovieContract.MovieEntry.COLUMN_TMDB_ID + " = ?",
                new String[]{Long.toString(movie.getId())}, null);
        if(cursor == null) {
            Log.w(LOG_TAG, "Error accessing database! Unable to store movie: " + movie.getId());
            return;
        }

        ContentValues values = new ContentValues();
        values.put(MovieContract.MovieEntry.COLUMN_TMDB_ID, movie.getId());
        values.put(MovieContract.MovieEntry.COLUMN_DESC, movie.getOverview());
        values.put(MovieContract.MovieEntry.COLUMN_IMG_PATH, movie.getPosterPath());
        values.put(MovieContract.MovieEntry.COLUMN_RATING, movie.getRating());
        values.put(MovieContract.MovieEntry.COLUMN_RELEASE, movie.getReleaseDate());
        values.put(MovieContract.MovieEntry.COLUMN_POPULARITY, movie.getPopularity());
        values.put(MovieContract.MovieEntry.COLUMN_RUNTIME, movie.getRunTime());
        values.put(MovieContract.MovieEntry.COLUMN_TITLE, movie.getTitle());
        values.put(MovieContract.MovieEntry.COLUMN_VOTE_CNT, movie.getVoteCount());
        values.put(MovieContract.MovieEntry.COLUMN_GENRES, encodeGenres(movie.getGenres()));

        if(cursor.getCount() == 0 || !cursor.moveToFirst()) {
            storeMovie(context, values);
        } else {
            updateMovie(context, values, movie.getId());
        }
        cursor.close();
    }

    private static void storeMovie(@NonNull Context context, @NonNull ContentValues values)
    {
        context.getContentResolver().insert(MovieContract.MovieEntry.CONTENT_URI, values);
    }

    private static void updateMovie(@NonNull Context context,
                                    @NonNull ContentValues values, Long id)
    {
        context.getContentResolver().update(MovieContract.MovieEntry.CONTENT_URI, values,
                MovieContract.MovieEntry.COLUMN_TMDB_ID + " = ?", new String[]{Long.toString(id)});
    }
}
