package com.example.android.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity
        implements DiscoverFragment.OnMovieSelectedListener
{
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    DetailFragment details;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        if (savedInstanceState == null) {
            getSupportActionBar().setElevation(8);
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction transaction = fm.beginTransaction();
            DiscoverFragment discoverFragment = new DiscoverFragment();
            transaction.add(R.id.container, discoverFragment);
            transaction.commit();

            if(this.findViewById(R.id.detail_container) != null)
            {
                transaction = fm.beginTransaction();
                details = new DetailFragment();
                transaction.add(R.id.detail_container, details);
                transaction.commit();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onMovieSelected(Movie movie)
    {
        Log.d(LOG_TAG, "Movie Selected: " + movie.getTitle());
        if(details == null) {
            details = new DetailFragment();
        }
        details.receiveMovie(movie);
        FragmentManager fm = this.getSupportFragmentManager();
        FragmentTransaction transaction =fm.beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.replace(R.id.container, details);
        transaction.addToBackStack("Replacement");
        transaction.commit();
    }
}
