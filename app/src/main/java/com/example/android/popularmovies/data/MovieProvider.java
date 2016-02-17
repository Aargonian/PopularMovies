package com.example.android.popularmovies.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;

/**
 * Created by Aaron Helton on 2/16/2016
 */
public class MovieProvider extends ContentProvider
{
    @SuppressWarnings("unused")
    private static final String LOG_TAG = MovieProvider.class.getSimpleName();

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private MovieDbHelper mOpenHelper;

    public static final int MOVIES = 100;
    public static final int MOVIE_WITH_ID = 101;

    public static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MovieContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, MovieContract.PATH_MOVIES, MOVIES);
        matcher.addURI(authority, MovieContract.PATH_MOVIES + "/#", MOVIE_WITH_ID);
        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new MovieDbHelper(getContext());
        return true;
    }

    @Override
    public String getType(@NonNull Uri uri)
    {
        final int match = sUriMatcher.match(uri);

        switch(match) {
            case MOVIES:
                return MovieContract.MovieEntry.CONTENT_TYPE;
            case MOVIE_WITH_ID:
                return MovieContract.MovieEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder)
    {
        Cursor retCursor;
        switch(sUriMatcher.match(uri)) {
            case MOVIES:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MovieContract.MovieEntry.TABLE_NAME,
                        projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case MOVIE_WITH_ID:
                retCursor = getMovieById(uri, projection);
                break;
            default:
                throw new UnsupportedOperationException("Unknown URI: " + uri);
        }

        //noinspection ConstantConditions
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs)
    {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        final int match = sUriMatcher.match(uri);
        int rowsModified;
        switch(match) {
            case MOVIES: {
                rowsModified = db.update(MovieContract.MovieEntry.TABLE_NAME, values,
                                         selection, selectionArgs);
                break;
            }
            default: {
                throw new UnsupportedOperationException("Invalid URI: " + uri);
            }
        }
        if(rowsModified > 0)
            //noinspection ConstantConditions
            getContext().getContentResolver().notifyChange(uri, null);
        return rowsModified;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;
        switch(match)
        {
            case MOVIES: {
                long id = values.getAsInteger(MovieContract.MovieEntry.COLUMN_TMDB_ID);
                long rowId = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, values);
                if(rowId > 0)
                    returnUri = MovieContract.MovieEntry.buildMovieUriWithId(id);
                else
                    throw new SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown URI: " + uri);
        }

        //noinspection ConstantConditions
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        if(selection == null)
            selection = "1";
        int rowsModified;
        switch(match) {
            case MOVIES: {
                rowsModified = db.delete(MovieContract.MovieEntry.TABLE_NAME,
                                         selection, selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Invalid URI: " + uri);
        }
        if(rowsModified > 0)
            //noinspection ConstantConditions
            getContext().getContentResolver().notifyChange(uri, null);
        return rowsModified;
    }

    private Cursor getMovieById(Uri uri, String[] projection)
    {
        long id = MovieContract.MovieEntry.getMovieIdFromUri(uri);
        return mOpenHelper.getReadableDatabase().query(
                MovieContract.MovieEntry.TABLE_NAME,
                projection,
                MovieContract.MovieEntry.COLUMN_TMDB_ID + " = ?",
                new String[]{Long.toString(id)},
                null,
                null,
                null
        );
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case MOVIES:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                //noinspection ConstantConditions
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}
