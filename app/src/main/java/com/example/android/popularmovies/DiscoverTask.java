package com.example.android.popularmovies;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.example.android.popularmovies.data.FileUtils;
import com.example.android.popularmovies.data.MovieContract;
import com.nytegear.android.network.NetworkUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by aargonian on 2/16/16.
 *
 * The DiscoverTask is responsible for fetching movies from theMovieDb's /discover/movie endpoint.
 * What the task does is it first downloads a list of movies at the specified page from the endpoint
 * Once the movies have been pulled down, it parses each id in the list, and checks to see if that
 * movie already exists in the database. If it doesn't, it silently downloads the movie and puts it
 * in the database. Either way, it then calls the publicProgress method with the TMDB_ID of the
 * movie, which can be used to fetch it from the DB for any activity/fragment listening.
 */
public class DiscoverTask extends AsyncTask<Integer, String, Void>
{
    private final String LOG_TAG = DiscoverTask.class.getSimpleName();
    private final String TMDB_DISCOVER_URL = Movie.TMDB_URL_BASE + "/discover/movie?";
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
        Log.d(LOG_TAG, "Getting Movies with Available Network!");
        //Get Sort Parameter
        String SORT = Utility.getSort(mContext);
        if(SORT.equalsIgnoreCase(mContext.getString(R.string.pref_sort_popularity_value)))
            SORT = "popularity.desc";
        else if (SORT.equalsIgnoreCase(mContext.getString(R.string.pref_sort_rating_value)))
            SORT = "vote_average.desc";
        else
            SORT = "release_date.desc";

        try
        {
            Uri.Builder builder = Uri.parse(TMDB_DISCOVER_URL).buildUpon()
                    .appendQueryParameter("sort_by", SORT)
                    .appendQueryParameter("api_key", Movie.API_KEY)
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
                        Movie.TMDB_URL_BASE + "! Retrieving movies from local database only.");
                getMoviesWithoutNetwork(page);
                return;
            }

            //Assuming all went well, we should have some JSON to parse now.
            JSONArray results = movieList.getJSONArray("results");
            for(int i = 0; i < results.length(); i++)
            {
                Integer id = results.getJSONObject(i).getInt("id");

                //Intentional violation of SRP here, to avoid querying the database twice for
                //imgPath. Instead, we can use the path itself to determine if it was in the db.
                String imgFilePath = existsInDatabase(id);
                if(imgFilePath == null) {
                    Movie movie = Movie.getMovie(id, mContext);
                    if(movie == null)
                        continue;
                    ContentValues values = new ContentValues();
                    try {
                        imgFilePath = FileUtils.storeImage(mContext,
                            Movie.getPoster(mContext,
                                    results.getJSONObject(i).getString("poster_path")),
                                id + ".png");
                    } catch (IOException ex) {
                        Log.e(LOG_TAG, "Error Getting/Storing Poster: " + ex.getMessage(), ex);
                        imgFilePath = FileUtils.storeImage(
                                mContext,
                                BitmapFactory.decodeResource(mContext.getResources(),
                                                             R.drawable.noimage),
                                id + ".png");
                    }
                    values.put(MovieContract.MovieEntry.COLUMN_TMDB_ID, id);
                    values.put(MovieContract.MovieEntry.COLUMN_DESC, movie.getOverview());
                    values.put(MovieContract.MovieEntry.COLUMN_IMG_PATH, imgFilePath);
                    values.put(MovieContract.MovieEntry.COLUMN_RATING, movie.getRating());
                    values.put(MovieContract.MovieEntry.COLUMN_RELEASE, movie.getReleaseDate());
                    values.put(MovieContract.MovieEntry.COLUMN_POPULARITY,
                               results.getJSONObject(i).getDouble("popularity"));
                    values.put(MovieContract.MovieEntry.COLUMN_RUNTIME, movie.getRunTime());
                    values.put(MovieContract.MovieEntry.COLUMN_TITLE, movie.getTitle());
                    values.put(MovieContract.MovieEntry.COLUMN_VOTE_CNT, movie.getVoteCount());
                    values.put(MovieContract.MovieEntry.COLUMN_GENRES,
                               parseGenres(movie.getGenres()));

                    mContext.getContentResolver().insert(MovieContract.MovieEntry.CONTENT_URI,
                                                         values);
                } else {
                    Log.v(LOG_TAG, "IT'S IN THE DATABASE! WHOO!");
                }
                publishProgress(Long.toString(id), imgFilePath);
            }
        } catch (MalformedURLException ex) {
            Log.e(LOG_TAG, "Malformed URL: " + ex.getMessage(), ex);
        } catch (JSONException ex) {
            Log.e(LOG_TAG, "Malformed JSON: " + ex.getMessage(), ex);
        }
    }

    private String parseGenres(String[] genres)
    {
        StringBuilder genreEntry = new StringBuilder();
        for(int i = 0; i < genres.length; i++)
        {
            genreEntry.append(genres[i]);
            if(i != genres.length-1)
                genreEntry.append('_');
        }
        return genreEntry.toString();
    }

    private String existsInDatabase(int id)
    {
        Cursor cursor =
                mContext.getContentResolver().query(
                        MovieContract.MovieEntry.CONTENT_URI,
                        null,
                        MovieContract.MovieEntry.COLUMN_TMDB_ID + " = ?",
                        new String[]{Long.toString(id)},
                        null
        );
        if(cursor == null || !cursor.moveToFirst()) {
            if(cursor != null)
                cursor.close();
            return null;
        } else {
            String path = cursor.getString(
                    cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_IMG_PATH));
            cursor.close();
            return path;

        }
    }

    private void getMoviesWithoutNetwork(int page) {
        long startRow = page * 25;
        long endRow = (page + 1) * 25;

        String sort = Utility.getSort(mContext);

        if (sort.equalsIgnoreCase(mContext.getString(R.string.pref_sort_rating_value))) {
            sort = MovieContract.MovieEntry.COLUMN_RATING + " DESC";
        } else {
            sort = MovieContract.MovieEntry.COLUMN_POPULARITY + " DESC";
        }

        Cursor cursor = mContext.getContentResolver().query(
                MovieContract.MovieEntry.CONTENT_URI,
                new String[]{MovieContract.MovieEntry._ID, MovieContract.MovieEntry.COLUMN_TMDB_ID,
                             MovieContract.MovieEntry.COLUMN_IMG_PATH},
                MovieContract.MovieEntry._ID + " >= ? AND " + MovieContract.MovieEntry._ID + " < ?",
                new String[]{Long.toString(startRow), Long.toString(endRow)},
                sort
        );

        if(!(cursor == null || !cursor.moveToFirst())) {
            int idIndex = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_TMDB_ID);
            int imgIndex = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_IMG_PATH);
            for(int i = 0; i < cursor.getCount(); i++)
            {
                publishProgress(Long.toString(cursor.getLong(idIndex)), cursor.getString(imgIndex));
                cursor.moveToNext();
            }
        }
    }
}