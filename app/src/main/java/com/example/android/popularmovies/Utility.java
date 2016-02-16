package com.example.android.popularmovies;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Aaron Helton on 2/16/2016
 */
public class Utility
{
    private static String prevSort;
    private static boolean sortChanged;

    public static String getSort(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String sort = prefs.getString(context.getString(R.string.pref_sort_key),
                                      context.getString(R.string.pref_sort_default));
        if(!sort.equalsIgnoreCase(prevSort)) {
            prevSort = sort;
            sortChanged = true;
        }
        return sort;
    }
}
