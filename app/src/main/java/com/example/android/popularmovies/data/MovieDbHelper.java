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
    private static final int DB_VER = 1;
    static final String DB_NAME = "popular_movies.db";

    public MovieDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VER);
    }

    //TODO: Create the Favorites, Rating, and Popular Tables
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_FAVORITES_TABLE = "CREATE TABLE " + MovieEntry.TABLE_NAME + " (" +
                MovieEntry._ID + " INTEGER PRIMARY KEY," +
                MovieEntry.COLUMN_TMDB_ID + " INTEGER UNIQUE NOT NULL," +
                MovieEntry.COLUMN_TITLE + " TEXT NOT NULL," +
                MovieEntry.COLUMN_DESC + " TEXT NOT NULL," +
                MovieEntry.COLUMN_IMG_PATH + " TEXT NOT NULL," +
                MovieEntry.COLUMN_RELEASE + " TEXT NOT NULL," +
                MovieEntry.COLUMN_RUNTIME + " INTEGER NOT NULL," +
                MovieEntry.COLUMN_VOTE_CNT + " INTEGER NOT NULL," +
                MovieEntry.COLUMN_RATING + " REAL NOT NULL," +
                MovieEntry.COLUMN_GENRES + " TEXT NOT NULL" +
                " );";
        sqLiteDatabase.execSQL(SQL_CREATE_FAVORITES_TABLE);
    }

    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        //TODO: Implement the onUpgrade Function along with Functional Tests
    }
}