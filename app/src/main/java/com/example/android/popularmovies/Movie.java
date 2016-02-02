package com.example.android.popularmovies;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by mint on 1/30/16.
 */
public class Movie implements Comparable<Movie>
{
    private static final String LOG_TAG = Movie.class.getSimpleName();
    public static final String API_KEY = BuildConfig.TMDBKey;
    //public static final String API_KEY = "6de0f0590c832161c5da34a0b67f23d5";
    public static final String TMDB_URL_BASE = "http://api.themoviedb.org/3";
    public static final String TMDB_POSTER_URL = "http://image.tmdb.org/t/p/w500";

    public static final Movie getMovie(Integer id, Context context, String API_KEY)
    {
        String url = TMDB_URL_BASE+"/movie/"+id +"?api_key="+API_KEY;
        String movieJSON = NetworkUtil.getURL(url);
        if(movieJSON == null || movieJSON.isEmpty())
            return null;

        try {
            JSONObject movieObject = new JSONObject(movieJSON);
            JSONArray genreList = movieObject.getJSONArray("genres");
            String[] genres = new String[genreList.length()];
            for(int i = 0; i < genreList.length(); i++)
                genres[i] = genreList.getJSONObject(i).getString("name");
            String title = movieObject.getString("original_title");
            String overview = movieObject.getString("overview");
            String posterPath = movieObject.getString("poster_path");
            String releaseDate = movieObject.getString("release_date");
            int runTime = movieObject.getInt("runtime");
            double vote = movieObject.getDouble("vote_average");
            int votes = movieObject.getInt("vote_count");

            Bitmap poster = null;
            try {
                poster = Picasso.with(context).load(TMDB_POSTER_URL + posterPath).get();
            } catch (IOException ex) {
                Log.e(LOG_TAG, "Unable To Get Movie Poster: " + ex.getMessage(), ex);
            }

            Movie movie = new Movie(title, overview, poster,
                                    releaseDate, runTime, vote, votes, genres);
            return movie;
        } catch (JSONException ex) {
            Log.e(LOG_TAG, "ERROR PARSING MOVIE: " + ex.getMessage(), ex);
            return null;
        }
    }

    private String title;
    private String overview;
    private Bitmap poster;
    private String releaseDate;
    private Integer runTime;
    private Integer vote_count;
    private Double rating;
    private String[] genres;

    public Movie(String title, String overview, Bitmap poster, String releaseDate,
                 Integer runTime, Double rating, Integer vote_count, String[] genreList)
    {
        this.title = title;
        this.overview = overview;
        this.poster = poster;
        this.releaseDate = releaseDate;
        this.runTime = runTime;
        this.rating = rating;
        this.vote_count = vote_count;
        this.genres = genreList;
    }

    public int compareTo(Movie other)
    {
        if(this.rating < other.rating) {
            return 1;
        }
        else if(this.rating > other.rating) {
            return -1;
        }
        else {
            if(this.vote_count < other.vote_count) {
                return 1;
            } else if (this.vote_count > other.vote_count) {
                return -1;
            } else {
                return 0;
            }
        }
    }

    public void applyPoster(ImageView view)
    {
        view.setImageBitmap(poster);
    }

    public String getTitle() {
        return title;
    }

    public String getOverview() {
        return overview;
    }

    public Bitmap getPoster() {
        return poster;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public Integer getRunTime() {
        return runTime;
    }

    public Double getRating() {
        return rating;
    }

    public Integer getVoteCount() { return vote_count; }

    public String[] getGenres() {
        return genres;
    }
}
