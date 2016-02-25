package com.example.android.popularmovies.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.util.Log;

import java.util.HashSet;

/**
 * Created by Aaron Helton on 2/15/2016
 */
public class TestDb extends AndroidTestCase {

    public static final String LOG_TAG = TestDb.class.getSimpleName();

    void deleteTheDatabase() {
        mContext.deleteDatabase(MovieDbHelper.DB_NAME);
    }

    @Override
    public void setUp() {
        deleteTheDatabase();
    }

    @Override
    public void tearDown() {
        deleteTheDatabase();
    }

    public void testCreateDb() throws Throwable {
        Log.d(LOG_TAG, "Test: testCreateDb()");
        final HashSet<String> tableNameHashSet = new HashSet<>();
        tableNameHashSet.add(MovieContract.MovieEntry.TABLE_NAME);

        mContext.deleteDatabase(MovieDbHelper.DB_NAME);
        SQLiteDatabase db = new MovieDbHelper(this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());

        // have we created the tables we want?
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        assertTrue("Error: This means that the database has not been created correctly",
                c.moveToFirst());

        // verify that the tables have
        // +been created
        do {
            tableNameHashSet.remove(c.getString(0));
        } while( c.moveToNext() );

        // if this fails, it means that your database doesn't contain both the location entry
        // and MovieInfo entry tables
        assertTrue("Error: Your database was created without the MovieInfo Table",
                tableNameHashSet.isEmpty());

        // now, do our tables contain the correct columns?
        c = db.rawQuery("PRAGMA table_info("+MovieContract.MovieEntry.TABLE_NAME + ")", null);

        assertTrue("Error: This means that we were unable to query " +
                        "the database for table information.",
                    c.moveToFirst());

        // Build a HashSet of all of the column names we want to look for
        final HashSet<String> movieColumnHashSet = new HashSet<>();
        movieColumnHashSet.add(MovieContract.MovieEntry._ID);
        movieColumnHashSet.add(MovieContract.MovieEntry.COLUMN_TMDB_ID);
        movieColumnHashSet.add(MovieContract.MovieEntry.COLUMN_TITLE);
        movieColumnHashSet.add(MovieContract.MovieEntry.COLUMN_DESC);
        movieColumnHashSet.add(MovieContract.MovieEntry.COLUMN_IMG_PATH);
        movieColumnHashSet.add(MovieContract.MovieEntry.COLUMN_RELEASE);
        movieColumnHashSet.add(MovieContract.MovieEntry.COLUMN_RUNTIME);
        movieColumnHashSet.add(MovieContract.MovieEntry.COLUMN_RATING);
        movieColumnHashSet.add(MovieContract.MovieEntry.COLUMN_FAVORITE);
        movieColumnHashSet.add(MovieContract.MovieEntry.COLUMN_VOTE_CNT);
        movieColumnHashSet.add(MovieContract.MovieEntry.COLUMN_GENRES);

        int columnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(columnNameIndex);
            movieColumnHashSet.remove(columnName);
        } while(c.moveToNext());

        // if this fails, it means that your database doesn't contain all of the required location
        // entry columns
        assertTrue("Error: The database doesn't contain all of the required location entry columns",
                 movieColumnHashSet.isEmpty());
        db.close();
    }

    //TODO: Add Test for Popular Table
    //TODO: Add Test for Rating Table
    //TODO: Add Test for Favorites Table
    public void testMovieTable() {
        Log.d(LOG_TAG, "TEST: TestDb.testMovieTable()");
        MovieDbHelper dbHelper = new MovieDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String filePath = TestUtilities.storeTestImage(mContext);
        ContentValues testValues = TestUtilities.createFightClubMovieValues(filePath);
        assertTrue(db.insert(MovieContract.MovieEntry.TABLE_NAME, null, testValues) != -1);
        Cursor cursor = db.query(MovieContract.MovieEntry.TABLE_NAME,
                                 null, null, null, null, null, null);
        assertTrue("Error: No Movies Returned From Query!", cursor.moveToFirst());
        TestUtilities.validateCurrentRecord("Error: MovieInfo Not Validated!", cursor, testValues);

        //Test bitmap
        String f =cursor.getString(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_IMG_PATH));
        assertTrue(f.equals(filePath));
        assertTrue(TestUtilities.getTestImage(mContext).sameAs(FileUtils.getImage(f)));

        assertFalse( "Error: More than one record!", cursor.moveToNext());
        cursor.close();
        db.close();
    }
}
