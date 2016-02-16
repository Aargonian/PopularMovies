package com.example.android.popularmovies.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Aaron Helton on 2/15/2016
 */
public class MovieContract  {
    public static final String CONTENT_AUTHORITY = "com.example.android.popularmovies";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_FAVORITES = "favorites";
    public static final String PATH_POPULAR = "popular";
    public static final String PATH_RATING = "rating";
    public static final String PATH_MOVIES = "movie";

    public static final class MovieEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIES).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIES;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIES;

        public static final String TABLE_NAME = "movies";
        public static final String COLUMN_TMDB_ID = "tmdb_id";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_DESC = "overview";
        public static final String COLUMN_POPULARITY = "popularity";
        public static final String COLUMN_IMG_PATH = "img_path";
        public static final String COLUMN_FAVORITE = "favorite";
        public static final String COLUMN_RELEASE = "release_date";
        public static final String COLUMN_RUNTIME = "runtime";
        public static final String COLUMN_VOTE_CNT = "vote_count";
        public static final String COLUMN_RATING = "rating";
        public static final String COLUMN_GENRES = "genres";

        public static Uri buildMovieUriWithId(long tmdb_id) {
            return ContentUris.withAppendedId(CONTENT_URI, tmdb_id);
        }

        public static long getMovieIdFromUri(Uri uri)
        {
            return Long.parseLong(uri.getPathSegments().get(uri.getPathSegments().size()-1));
        }
    }
}