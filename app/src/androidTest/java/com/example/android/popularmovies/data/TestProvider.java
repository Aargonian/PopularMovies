package com.example.android.popularmovies.data;

import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.test.AndroidTestCase;
import android.util.Log;

import com.example.android.popularmovies.data.MovieContract.MovieEntry;

public class TestProvider extends AndroidTestCase {

    public static final String LOG_TAG = TestProvider.class.getSimpleName();

    public void deleteAllRecords() {
        Log.d(LOG_TAG, "TEST: TestProvider.deleteAllRecords()");
        mContext.getContentResolver().delete(
                MovieEntry.CONTENT_URI,
                null,
                null
        );

        Cursor cursor = mContext.getContentResolver().query(
                MovieEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        assertNotNull(cursor);
        if(cursor.moveToFirst())
        {
            int columns = cursor.getColumnCount();
            Log.e(LOG_TAG, "DETECTED VALUES:\n");
            for(int i = 0; i < columns; i++)
            {
                Log.e(LOG_TAG, cursor.getColumnName(i) + ": ");
            }
            Log.e(LOG_TAG, "SUSPECT FAVORITE? " + cursor.getInt(cursor.getColumnIndex(MovieEntry.COLUMN_FAVORITE)));
        }
        assertEquals("Error: Movie Records Not Deleted!", 0, cursor.getCount());
        cursor.close();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        deleteAllRecords();
    }

    /*
        This test checks to make sure that the content provider is registered correctly.
        Students: Uncomment this test to make sure you've correctly registered the MovieProvider.
     */
    public void testProviderRegistry() {
        Log.d(LOG_TAG, "TEST: TestProvider.testProviderRegistry()");
        PackageManager pm = mContext.getPackageManager();
        ComponentName componentName =
                new ComponentName(mContext.getPackageName(), MovieProvider.class.getName());
        try {
            ProviderInfo providerInfo = pm.getProviderInfo(componentName, 0);
            assertEquals("Error: MovieProvider registered with authority: " + providerInfo.authority +
                    " instead of authority: " + MovieContract.CONTENT_AUTHORITY,
                    providerInfo.authority, MovieContract.CONTENT_AUTHORITY);
        } catch (PackageManager.NameNotFoundException e) {
            assertTrue("Error: MovieProvider not registered at " + mContext.getPackageName(),false);
        }
    }

    public void testGetType() {
        Log.d(LOG_TAG, "TEST: TestProvider.testGetType()");
        String type = mContext.getContentResolver().getType(MovieEntry.CONTENT_URI);
        assertEquals("Error: the MovieEntry CONTENT_URI should return MovieEntry.CONTENT_TYPE",
                MovieEntry.CONTENT_TYPE, type);

        long testId = 550;
        type = mContext.getContentResolver().getType(MovieEntry.buildMovieUriWithId(testId));

        assertEquals("Error: the MovieEntry CONTENT_URI WITH ID " +
                "should return MovieEntry.CONTENT_ITEM_TYPE", MovieEntry.CONTENT_ITEM_TYPE, type);
    }

    public void testBasicMovieQuery() {
        Log.d(LOG_TAG, "TEST: TestProvider.testBasicMovieQuery()");
        MovieDbHelper dbHelper = new MovieDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String filePath = TestUtilities.storeTestImage(mContext);
        ContentValues MovieValues = TestUtilities.createFightClubMovieValues(filePath);
        long MovieRowId = db.insert(MovieEntry.TABLE_NAME, null, MovieValues);
        assertTrue("Unable to Insert MovieEntry into the Database", MovieRowId != -1);
        db.close();

        Cursor MovieCursor = mContext.getContentResolver().query(
                MovieEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        TestUtilities.validateCursor("testBasicMovieQuery", MovieCursor, MovieValues);
    }

    public void testUpdateMovie() {
        Log.d(LOG_TAG, "TEST: TestProvider.testUpdateMovie()");
        String filePath = TestUtilities.storeTestImage(mContext);
        ContentValues values = TestUtilities.createFightClubMovieValues(filePath);

        Uri movieUri = mContext.getContentResolver().insert(MovieEntry.CONTENT_URI, values);
        long movieTMDBId = ContentUris.parseId(movieUri);

        Log.d(LOG_TAG, "Movie ID: " + movieTMDBId);

        ContentValues updatedValues = new ContentValues(values);
        updatedValues.put(MovieEntry.COLUMN_POPULARITY, 25.3);

        // Create a cursor with observer to make sure that the content provider is notifying
        // the observers as expected
        Cursor movieCursor = mContext.getContentResolver().query(MovieEntry.CONTENT_URI,
                                                                    null, null, null, null);
        assertNotNull(movieCursor);
        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();
        movieCursor.registerContentObserver(tco);

        int count = mContext.getContentResolver().update(
                MovieEntry.CONTENT_URI, updatedValues, MovieEntry.COLUMN_TMDB_ID + " = ?",
                new String[] { Long.toString(movieTMDBId)});
        assertEquals(1, count);

        tco.waitForNotificationOrFail();
        movieCursor.unregisterContentObserver(tco);
        movieCursor.close();

        Cursor cursor = mContext.getContentResolver().query(
                MovieEntry.CONTENT_URI,
                null,   // projection
                MovieEntry.COLUMN_TMDB_ID + " = " + movieTMDBId,
                null,   // Values for the "where" clause
                null    // sort order
        );
        assertNotNull(cursor);
        TestUtilities.validateCursor("testUpdateLocation.  Error validating location entry update.",
                cursor, updatedValues);

        cursor.close();
    }

    public void testInsertReadProvider() {
        Log.d(LOG_TAG, "TEST: TestProvider.testInsertReadProvider()");
        String filePath = TestUtilities.storeTestImage(mContext);
        ContentValues testValues = TestUtilities.createFightClubMovieValues(filePath);

        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(MovieEntry.CONTENT_URI, true, tco);
        Uri movieUri = mContext.getContentResolver().insert(MovieEntry.CONTENT_URI, testValues);

        tco.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(tco);

        long movieRowId = ContentUris.parseId(movieUri);

        assertTrue(movieRowId != -1);

        Cursor cursor = mContext.getContentResolver().query(
                MovieEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        TestUtilities.validateCursor("testInsertReadProvider. Error validating MovieEntry.",
                cursor, testValues);
    }

    public void testDeleteRecords() {
        testInsertReadProvider();

        TestUtilities.TestContentObserver MovieObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(MovieEntry.CONTENT_URI, true, MovieObserver);

        deleteAllRecords();

        MovieObserver.waitForNotificationOrFail();

        mContext.getContentResolver().unregisterContentObserver(MovieObserver);
    }


    static private final int BULK_INSERT_RECORDS_TO_INSERT = 10;
    static ContentValues[] createBulkInsertMovieValues(Context context) {
        String movie = TestUtilities.storeTestImage(context);
        ContentValues[] returnContentValues = new ContentValues[BULK_INSERT_RECORDS_TO_INSERT];
        long startID = 1;
        for ( int i = 0; i < BULK_INSERT_RECORDS_TO_INSERT; i++) {
            ContentValues MovieValues = new ContentValues();
            MovieValues.put(MovieContract.MovieEntry.COLUMN_TMDB_ID, startID++);
            MovieValues.put(MovieContract.MovieEntry.COLUMN_TITLE, "Game Grumps: The Movie");
            MovieValues.put(MovieContract.MovieEntry.COLUMN_DESC, "Two men, one game, 10 minutes");
            MovieValues.put(MovieContract.MovieEntry.COLUMN_POPULARITY, 9001);
            MovieValues.put(MovieContract.MovieEntry.COLUMN_IMG_PATH, movie);
            MovieValues.put(MovieContract.MovieEntry.COLUMN_FAVORITE, 1);
            MovieValues.put(MovieContract.MovieEntry.COLUMN_RELEASE, "2012-07-18");
            MovieValues.put(MovieContract.MovieEntry.COLUMN_RUNTIME, 18);
            MovieValues.put(MovieContract.MovieEntry.COLUMN_VOTE_CNT, 3047171);
            MovieValues.put(MovieContract.MovieEntry.COLUMN_RATING, 11.2);
            MovieValues.put(MovieContract.MovieEntry.COLUMN_GENRES, "Comedy_Romance_Drama");
            returnContentValues[i] = MovieValues;
        }
        return returnContentValues;
    }

    public void testBulkInsert() {
        ContentValues[] bulkInsertContentValues = createBulkInsertMovieValues(mContext);

        // Register a content observer for our bulk insert.
        TestUtilities.TestContentObserver movieObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver()
                .registerContentObserver(MovieEntry.CONTENT_URI, true, movieObserver);

        int insertCount = mContext.getContentResolver().bulkInsert(MovieEntry.CONTENT_URI,
                bulkInsertContentValues);

        movieObserver.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(movieObserver);

        assertEquals(insertCount, BULK_INSERT_RECORDS_TO_INSERT);

        Cursor cursor = mContext.getContentResolver().query(
                MovieEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertNotNull(cursor);
        assertEquals(cursor.getCount(), BULK_INSERT_RECORDS_TO_INSERT);

        cursor.moveToFirst();
        for ( int i = 0; i < BULK_INSERT_RECORDS_TO_INSERT; i++, cursor.moveToNext() ) {
            TestUtilities.validateCurrentRecord("testBulkInsert.  Error validating MovieEntry " + i,
                    cursor, bulkInsertContentValues[i]);
        }
        cursor.close();
    }
}
