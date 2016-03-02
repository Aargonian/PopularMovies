package com.example.android.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.popularmovies.data.MovieContract.*;
/**
 * Created by Aaron Helton on 2/15/2016
 */
//TODO: Create Tests for MovieDbHelper
public class MovieDbHelper extends SQLiteOpenHelper {
    private static final int DB_VER = 3;
    static final String DB_NAME = "popular_movies.db";

    public MovieDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VER);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_MOVIES_TABLE = "CREATE TABLE " + MovieEntry.TABLE_NAME + " (" +
                MovieEntry._ID + " INTEGER PRIMARY KEY," +
                MovieEntry.COLUMN_TMDB_ID + " INTEGER UNIQUE NOT NULL," +
                MovieEntry.COLUMN_TITLE + " TEXT NOT NULL," +
                MovieEntry.COLUMN_DESC + " TEXT NOT NULL," +
                MovieEntry.COLUMN_POPULARITY + " REAL NOT NULL," +
                MovieEntry.COLUMN_IMG_PATH + " TEXT NOT NULL," +
                MovieEntry.COLUMN_RELEASE + " TEXT NOT NULL," +
                MovieEntry.COLUMN_RUNTIME + " INTEGER NOT NULL," +
                MovieEntry.COLUMN_FAVORITE + " INTEGER NOT NULL DEFAULT 0," +
                MovieEntry.COLUMN_VOTE_CNT + " INTEGER NOT NULL," +
                MovieEntry.COLUMN_RATING + " REAL NOT NULL," +
                MovieEntry.COLUMN_GENRES + " TEXT NOT NULL," +
                MovieEntry.COLUMN_REVIEWS + " TEXT," +
                MovieEntry.COLUMN_TRAILERS + " TEXT" +
                " );";
        sqLiteDatabase.execSQL(SQL_CREATE_MOVIES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        //No upgrade because no public version of the app has been released, and no updated db
        if(oldVersion < 2) {
            final String ADD_FAVORITES = "ALTER TABLE " + MovieEntry.TABLE_NAME + " ADD COLUMN " +
                    MovieEntry.COLUMN_FAVORITE + " INT NOT NULL DEFAULT 0";
            sqLiteDatabase.execSQL(ADD_FAVORITES);
        }
        if(oldVersion < 3) {
            final String ADD_REVIEWS = "ALTER TABLE " + MovieEntry.TABLE_NAME + " ADD COLUMN " +
                    MovieEntry.COLUMN_REVIEWS + " TEXT";
            sqLiteDatabase.execSQL(ADD_REVIEWS);

            final String ADD_TRAILERS = "ATLER TABLE " + MovieEntry.TABLE_NAME + " ADD COLUMN " +
                    MovieEntry.COLUMN_TRAILERS + " TEXT";
            sqLiteDatabase.execSQL(ADD_TRAILERS);
        }
    }
}