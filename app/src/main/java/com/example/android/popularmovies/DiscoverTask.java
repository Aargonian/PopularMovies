package com.example.android.popularmovies;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.example.android.popularmovies.data.MovieContract;
import com.example.android.popularmovies.data.MovieUtil;
import com.nytegear.android.network.NetworkUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by aargonian on 2/16/16.
 *
 * The DiscoverTask is responsible for fetching movies from theMovieDb's /discover/movie endpoint.
 * What the task does is it first downloads a list of movies at the specified page from the endpoint
 * Once the movies have been pulled down, it parses each id in the list, and hands it to MovieUtil.
 * MovieUtil will either download the movie or retrieve it from the database if it is already cached.
 * Once the movie has been retrieved, DiscoverTask then calls the publicProgress method with the
 * TMDB_ID of the movie.
 */
public class DiscoverTask extends AsyncTask<Integer, String, Void>
{
    private final String LOG_TAG = DiscoverTask.class.getSimpleName();
    private Context mContext;

    public DiscoverTask(Context context) {
        mContext = context;
    }

    @Override
    public Void doInBackground(Integer... params)
    {
        int page = 1;
        if(params.length > 0)
            page = params[0];

        if(NetworkUtil.isInternetAvailable(mContext)) {
            getMoviesWithNetworkAvailable(page);
        } else {
            getMoviesWithoutNetwork(page);
        }
        return null;
    }

    private void getMoviesWithNetworkAvailable(int page)
    {
        String SORT = Utility.getSort(mContext);
        if(SORT.equalsIgnoreCase(mContext.getString(R.string.pref_sort_popularity_value))) {
            SORT = "popularity.desc";
        } else if (SORT.equalsIgnoreCase(mContext.getString(R.string.pref_sort_rating_value))) {
            SORT = "vote_average.desc";
        } else {
            getMoviesWithoutNetwork(page); //Favorite sorting. Network not required.
            return;
        }

        try
        {
            String TMDB_DISCOVER_URL = MovieUtil.TMDB_URL_BASE + "/discover/movie?";
            Uri.Builder builder = Uri.parse(TMDB_DISCOVER_URL).buildUpon()
                    .appendQueryParameter("sort_by", SORT)
                    .appendQueryParameter("api_key", MovieUtil.API_KEY)
                    .appendQueryParameter("page", Integer.toString(page));

            //Hack to avoid strange results in vote average sorts.
            //TODO: Make this a setting.
            if(SORT.equals("vote_average.desc"))
                builder.appendQueryParameter("vote_count.gte", "250");
            Uri uri = builder.build();
            String moviesJSON = NetworkUtil.getURL(new URL(uri.toString()).toString());

            JSONObject movieList;
            if(moviesJSON != null)
                movieList = new JSONObject(moviesJSON);
            else {
                Log.e(LOG_TAG, "Catastrophe! We were unable to get the movies from " +
                        MovieUtil.TMDB_URL_BASE + "! Retrieving movies from local database only.");
                getMoviesWithoutNetwork(page);
                return;
            }

            //Assuming all went well, we should have some JSON to parse now.
            JSONArray results = movieList.getJSONArray("results");
            for(int i = 0; i < results.length(); i++)
            {
                Long id = results.getJSONObject(i).getLong("id");
                //Use the 'FromNet' version of GetMovie so we can update any relevant info as well.
                MovieInfo movie = MovieUtil.getMovieFromNet(mContext, id);
                if(movie == null) {
                    Log.w(LOG_TAG, "NULL MOVIE: " + id);
                    continue;
                }
                publishProgress(Long.toString(id), movie.getPosterPath());
            }
        } catch (MalformedURLException ex) {
            Log.e(LOG_TAG, "Malformed URL: " + ex.getMessage(), ex);
        } catch (JSONException ex) {
            Log.e(LOG_TAG, "Malformed JSON: " + ex.getMessage(), ex);
        }
    }

    private void getMoviesWithoutNetwork(int page) {
        page-=1;
        if(page < 0)
            page = 0;
        int startRow = page * 20;
        int endRow = (page + 1) * 20;

        String sort = Utility.getSort(mContext);

        if (sort.equalsIgnoreCase(mContext.getString(R.string.pref_sort_rating_value))) {
            sort = MovieContract.MovieEntry.COLUMN_RATING + " DESC";
        } else if (sort.equalsIgnoreCase(mContext.getString(R.string.pref_sort_popularity_value))){
            sort = MovieContract.MovieEntry.COLUMN_POPULARITY + " DESC";
        } else {
            sort = null;
        }

        String selection = null;
        if(sort == null)
        {
            //Favorites sort, so change the selection arguments
            selection = MovieContract.MovieEntry.COLUMN_FAVORITE + " = 1";
            //A bit arbitrarily, we'll sort favorites by rating
            sort = MovieContract.MovieEntry.COLUMN_RATING + " DESC";
        }

        //Grab a cursor with every movie in the database, initially. If doing favorites, it'll only
        //be the favorites. Other sorts will literally be every movie.\
        //We grab every movie initially because that allows the Database to sort the data by our
        //sort. We can then return just the movies we want by filtering by cursor position.
        Cursor cursor = mContext.getContentResolver().query(
                MovieContract.MovieEntry.CONTENT_URI,
                new String[]{MovieContract.MovieEntry.COLUMN_TMDB_ID, MovieContract.MovieEntry.COLUMN_IMG_PATH},
                selection,
                null,
                sort
        );

        if(cursor == null) {
            Log.w(LOG_TAG, "NULL CURSOR!");
            return;
        } else if (!cursor.moveToFirst()) {
            Log.w(LOG_TAG, "No rows returned from cursor!");
            cursor.close();
            return;
        } else if (cursor.getCount() <= startRow) {
            Log.w(LOG_TAG, "Requesting Pages Beyond Database Limits!");
            cursor.close();
            return;
        }

        int idIndex = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_TMDB_ID);
        int imgIndex = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_IMG_PATH);
        for(int i = startRow; i < endRow && i < cursor.getCount(); i++)
        {
            cursor.moveToPosition(i);
            publishProgress(Long.toString(cursor.getLong(idIndex)), cursor.getString(imgIndex));
            cursor.moveToNext();
        }
    }
}