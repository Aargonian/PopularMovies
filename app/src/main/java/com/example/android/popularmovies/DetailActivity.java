package com.example.android.popularmovies;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

/**
 * Created by Aaron Helton on 2/2/2016
 */
public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setTitle(this.getIntent().getStringExtra("MOVIE_TITLE"));
        setContentView(R.layout.activity_detail);
        getSupportFragmentManager().beginTransaction().add(R.id.detail_container,
                                                           new DetailFragment()).commit();
    }
}
