package com.example.android.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

/**
 * Created by Aaron Helton on 1/30/2016
 */
public class MainActivity extends AppCompatActivity
        implements DiscoverFragment.OnMovieSelectedListener
{
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private DetailFragment details;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        if (savedInstanceState == null) {
            if(getSupportActionBar() != null)
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

    @Override
    public void onMovieSelected(Long movieID)
    {
        if(findViewById(R.id.detail_container) != null && details == null)
        {
            details = new DetailFragment();
            FragmentManager fm = this.getSupportFragmentManager();
            FragmentTransaction transaction =fm.beginTransaction();
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            transaction.add(R.id.detail_container, details);
            transaction.addToBackStack("Replacement");
            transaction.commit();
        }

        if(findViewById(R.id.detail_container) != null)
        {
            details.setMovie(movieID);
        }
        else
        {
            Intent detailIntent = new Intent(this, DetailActivity.class);
            detailIntent.putExtra("MOVIE_ID", movieID);
            detailIntent.putExtra("DISPLAY_TITLE", true);
            this.startActivity(detailIntent);
        }
    }
}
