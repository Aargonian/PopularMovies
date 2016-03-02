package com.example.android.popularmovies.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
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

    private static final String[] projection = {
            MovieContract.MovieEntry.COLUMN_TITLE,
            MovieContract.MovieEntry.COLUMN_DESC,
            MovieContract.MovieEntry.COLUMN_IMG_PATH,
            MovieContract.MovieEntry.COLUMN_RELEASE,
            MovieContract.MovieEntry.COLUMN_RUNTIME,
            MovieContract.MovieEntry.COLUMN_RATING,
            MovieContract.MovieEntry.COLUMN_POPULARITY,
            MovieContract.MovieEntry.COLUMN_VOTE_CNT,
            MovieContract.MovieEntry.COLUMN_GENRES,
            MovieContract.MovieEntry.COLUMN_FAVORITE,
            MovieContract.MovieEntry.COLUMN_TRAILERS,
            MovieContract.MovieEntry.COLUMN_REVIEWS
    };

    @SuppressWarnings("all") private static final int TITLE = 0;
    @SuppressWarnings("all") private static final int OVERVIEW = 1;
    @SuppressWarnings("all") private static final int IMG_PATH = 2;
    @SuppressWarnings("all") private static final int RELEASE = 3;
    @SuppressWarnings("all") private static final int RUNTIME = 4;
    @SuppressWarnings("all") private static final int RATING = 5;
    @SuppressWarnings("all") private static final int POPULARITY = 6;
    @SuppressWarnings("all") private static final int VOTE_COUNT = 7;
    @SuppressWarnings("all") private static final int GENRES = 8;
    @SuppressWarnings("all") private static final int FAVORITE = 9;
    @SuppressWarnings("all") private static final int TRAILERS = 10;
    @SuppressWarnings("all") private static final int REVIEWS = 11;

    public static MovieInfo getMovie(@NonNull Context context, Long movieID)
    {
        Cursor cursor = context.getContentResolver().query(
                MovieContract.MovieEntry.CONTENT_URI,
                projection,
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
                    .withGenres(convertStringToArray(cursor.getString(GENRES)))
                    .withReviews(convertStringToArray(cursor.getString(REVIEWS)))
                    .withTrailers(convertStringToArray(cursor.getString(TRAILERS))).build();
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
        Uri movieURI = Uri.parse(TMDB_URL_BASE).buildUpon()
                .appendPath("movie")
                .appendPath(Long.toString(id))
                .appendQueryParameter("api_key", API_KEY)
                .appendQueryParameter("append_to_response", "reviews,videos").build();
        String movieJSON = null;
        try
        {
            movieJSON = NetworkUtil.getURL(movieURI.toString());
            if(movieJSON == null || movieJSON.isEmpty()) {
                Log.w(LOG_TAG, "Unable to fetch movie from network! TMDB ID: " + id);
                return null;
            }

            JSONObject movieObject = new JSONObject(movieJSON);
            JSONArray genreList = movieObject.getJSONArray("genres");
            String[] genres = new String[genreList.length()];
            for(int i = 0; i < genreList.length(); i++)
                genres[i] = genreList.getJSONObject(i).getString("name");
            String title = movieObject.getString("original_title");
            String overview = movieObject.getString("overview");
            String posterPath = movieObject.getString("poster_path");
            String releaseDate = movieObject.getString("release_date");

            System.out.println("MOVIE JSON: " + movieJSON);

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

            //GetReviews
            String[] reviews = null;
            JSONObject reviewObject = movieObject.getJSONObject("reviews");
            try {
                JSONArray reviewList = reviewObject.getJSONArray("results");
                if (reviewList.length() > 0) {
                    reviews = new String[reviewList.length() > 3 ? 3 : reviewList.length()];
                    for (int i = 0; i < reviewList.length() && i < 3; i++) {
                        reviews[i] = reviewList.getJSONObject(i).getString("content");
                    }
                }
            } catch (JSONException ex) {
                Log.e(LOG_TAG, "Error Retrieving Reviews: " + ex.getMessage(), ex);
                Log.e(LOG_TAG, "REVIEW JSON: " + reviewObject.toString());
            }

            //Get Trailers
            final String YOUTUBE_BASE_VIDEO_URL = "https://www.youtube.com/watch?v=";
            String[] trailers = null;
            JSONObject trailerObject = movieObject.getJSONObject("videos");
            try {
                JSONArray trailerList = trailerObject.getJSONArray("results");
                if (trailerList.length() > 0) {
                    trailers = new String[trailerList.length() > 3 ? 3 : trailerList.length()];
                    for (int i = 0; i < trailerList.length() && i < 3; i++) {
                        trailers[i] =
                                YOUTUBE_BASE_VIDEO_URL + trailerList.getJSONObject(i).getString("key");
                    }
                }
            } catch (JSONException ex) {
                Log.e(LOG_TAG, "ERROR GETTING TRAILERS: " + ex.getMessage(), ex);
                Log.e(LOG_TAG, "TRAILER JSON: " + trailerObject.toString());
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
                                         .withPosterPath(imgPath)
                                         .withReviews(reviews)
                                         .withTrailers(trailers).build();
            upsertMovie(context, info); //For caching purposes
            return info;
        } catch (JSONException ex) {
            Log.e(LOG_TAG, "ERROR PARSING MOVIE: " + ex.getMessage(), ex);
            Log.e(LOG_TAG, "JSON TEXT:\n"+movieJSON);
            return null;
        }
    }

    public static final String STR_SEPARATOR = "__,__";

    public static String convertArrayToString(String[] array){
        if(array == null)
            return null;
        StringBuilder builder = new StringBuilder();
        for (int i = 0;i<array.length; i++) {
            builder.append(array[i]);
            if(i<array.length-1){
                builder.append(STR_SEPARATOR);
            }
        }
        return builder.toString();
    }

    public static String[] convertStringToArray(String str){
        if(str == null)
            return null;
        return str.split(STR_SEPARATOR);
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
        values.put(MovieContract.MovieEntry.COLUMN_GENRES, convertArrayToString(movie.getGenres()));
        values.put(MovieContract.MovieEntry.COLUMN_REVIEWS,
                convertArrayToString(movie.getReviews()));
        values.put(MovieContract.MovieEntry.COLUMN_TRAILERS,
                convertArrayToString(movie.getTrailers()));

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
