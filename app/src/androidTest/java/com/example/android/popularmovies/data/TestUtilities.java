package com.example.android.popularmovies.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.test.AndroidTestCase;

import com.example.android.popularmovies.R;
import com.example.android.popularmovies.utils.PollingCheck;

import java.util.Map;
import java.util.Set;

public class TestUtilities extends AndroidTestCase {

    static String storeTestImage(Context context) {
        Bitmap bm = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
        return FileUtils.storeImage(context, bm, "FightClub.png");
    }

    static Bitmap getTestImage(Context context) {
        return BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
    }

    static void validateCursor(String error, Cursor valueCursor, ContentValues expectedValues) {
        assertTrue("Empty cursor returned. " + error, valueCursor.moveToFirst());
        validateCurrentRecord(error, valueCursor, expectedValues);
        valueCursor.close();
    }

    static void validateCurrentRecord(String error, Cursor valueCursor,ContentValues expectedValues)
    {
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse("Column '" + columnName + "' not found. " + error, idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals("Value '" + entry.getValue().toString() +
                    "' did not match the expected value '" +
                    expectedValue + "'. " + error, expectedValue, valueCursor.getString(idx));
        }
    }

    public static ContentValues createFightClubMovieValues(String imgPath) {
        ContentValues testValues = new ContentValues();
        testValues.put(MovieContract.MovieEntry.COLUMN_TMDB_ID, 550);
        testValues.put(MovieContract.MovieEntry.COLUMN_TITLE,"Fight Club");
        testValues.put(MovieContract.MovieEntry.COLUMN_DESC, "A ticking-time-bomb insomniac and a"+
                " slippery soap salesman channel primal male aggression into a shocking new form " +
                "of therapy. Their concept catches on, with underground \"fight clubs\" forming " +
                "in every town, until an eccentric gets in the way and ignites an out-of-control " +
                "spiral toward oblivion.");
        testValues.put(MovieContract.MovieEntry.COLUMN_POPULARITY, 2.5);
            //Can't use real double because of comparison problems.
        testValues.put(MovieContract.MovieEntry.COLUMN_IMG_PATH, imgPath);
        testValues.put(MovieContract.MovieEntry.COLUMN_RELEASE, "1999-10-14");
        testValues.put(MovieContract.MovieEntry.COLUMN_RUNTIME, 139);
        testValues.put(MovieContract.MovieEntry.COLUMN_FAVORITE, 1);
        testValues.put(MovieContract.MovieEntry.COLUMN_VOTE_CNT, 3185);
        testValues.put(MovieContract.MovieEntry.COLUMN_RATING, 7.7);
        testValues.put(MovieContract.MovieEntry.COLUMN_GENRES, "Drama");

        return testValues;
    }

    static class TestContentObserver extends ContentObserver {
        final HandlerThread mHT;
        boolean mContentChanged;

        static TestContentObserver getTestContentObserver() {
            HandlerThread ht = new HandlerThread("ContentObserverThread");
            ht.start();
            return new TestContentObserver(ht);
        }

        private TestContentObserver(HandlerThread ht) {
            super(new Handler(ht.getLooper()));
            mHT = ht;
        }

        // On earlier versions of Android, this onChange method is called
        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            mContentChanged = true;
        }

        public void waitForNotificationOrFail() {
            // Note: The PollingCheck class is taken from the Android CTS (Compatibility Test Suite).
            // It's useful to look at the Android CTS source for ideas on how to test your Android
            // applications.  The reason that PollingCheck works is that, by default, the JUnit
            // testing framework is not running on the main Android application thread.
            new PollingCheck(5000) {
                @Override
                protected boolean check() {
                    return mContentChanged;
                }
            }.run();
            mHT.quit();
        }
    }

    static TestContentObserver getTestContentObserver() {
        return TestContentObserver.getTestContentObserver();
    }
}
