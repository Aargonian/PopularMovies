package com.example.android.popularmovies;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;


/**
 * Created by Aaron Helton on 1/30/2016
 */
public class DiscoverFragment extends Fragment implements MovieAdapter.MovieItemClickListener
{
    private static final String LOG_TAG = DiscoverFragment.class.getSimpleName();
    private static final Integer PREFERRED_COLUMN_WIDTH = 100;
    private OnMovieSelectedListener mListener;
    private RecyclerView mMoviesGridView;
    private GridLayoutManager mManager;
    private MovieAdapter mAdapter;

    public DiscoverFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mAdapter = new MovieAdapter(this);
        DiscoverTask task = new DiscoverTask();
        task.execute();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_discover, container, false);
        FrameLayout movieContainer = (FrameLayout)root.findViewById(R.id.movieContainer);

        //Create Recyclerview Programmatically so that we can get an auto-fit-like functionality
        mMoviesGridView = new RecyclerView(this.getContext()) {
            @Override
            protected void onMeasure(int widthSpec, int heightSpec) {
                super.onMeasure(widthSpec, heightSpec);
                if(this.getLayoutManager() != null &&
                        this.getLayoutManager() instanceof GridLayoutManager)
                {
                    Resources resources = DiscoverFragment.this.getActivity().getResources();
                    DisplayMetrics metrics = resources.getDisplayMetrics();
                    float dp = getMeasuredWidth() / (metrics.densityDpi / 160f);
                    int spans = Math.max(1, (int)(dp / PREFERRED_COLUMN_WIDTH));
                    ((GridLayoutManager)this.getLayoutManager()).setSpanCount(spans);
                }
            }
        };
        mMoviesGridView.setLayoutParams(new RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.MATCH_PARENT,
                RecyclerView.LayoutParams.MATCH_PARENT));
        mMoviesGridView.setHorizontalScrollBarEnabled(false);
        mMoviesGridView.setVerticalScrollBarEnabled(false);
        mMoviesGridView.setHasFixedSize(true);
        mManager = new GridLayoutManager(this.getContext(), 1);
        mMoviesGridView.setLayoutManager(mManager);
        mMoviesGridView.setAdapter(mAdapter);
        movieContainer.addView(mMoviesGridView);
        return root;
    }

    @Override
    public void movieClicked(Integer id, String title)
    {
        mListener.onMovieSelected(id, title);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnMovieSelectedListener) {
            mListener = (OnMovieSelectedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnMovieSelectedListener {
        void onMovieSelected(Integer id, String title);
    }

    private class DiscoverTask extends AsyncTask<Void, MovieAdapter.MovieReference, Void>
    {
        private final String LOG_TAG = DiscoverTask.class.getSimpleName();

        @Override
        public Void doInBackground(Void... params)
        {
            String TMDB_DISCOVER_URL = Movie.TMDB_URL_BASE+"/discover/movie?";
            Context context = DiscoverFragment.this.getActivity();
            SharedPreferences prefs =
                    PreferenceManager
                            .getDefaultSharedPreferences(DiscoverFragment.this.getActivity());
            String SORT = prefs.getString(getString(R.string.pref_sort_key), getString(R.string.pref_sort_default));
            if(SORT.equalsIgnoreCase(getResources().getStringArray(R.array.pref_sort_values)[0]))
                SORT = "popularity.desc";
            else
                SORT = "vote_average.desc";
            try {
                Uri uri = Uri.parse(TMDB_DISCOVER_URL).buildUpon()
                        .appendQueryParameter("sort_by", SORT)
                        .appendQueryParameter("api_key", Movie.API_KEY).build();
                String moviesJSON = NetworkUtil.getURL(new URL(uri.toString()).toString());
                JSONObject movieList = new JSONObject(moviesJSON);
                JSONArray results = movieList.getJSONArray("results");
                for(int i = 0; i < results.length(); i++) {
                    Integer id = results.getJSONObject(i).getInt("id");
                    String title = results.getJSONObject(i).getString("original_title");
                    Bitmap poster = null;
                    try {
                        poster = Movie.getPoster(context,
                                        results.getJSONObject(i).getString("poster_path"));
                    } catch (IOException ex) {
                        Log.e(LOG_TAG, "Unable to retrive poster! " + ex.getMessage(), ex);
                    }
                    publishProgress(new MovieAdapter.MovieReference(id, poster, title));
                }
            } catch (MalformedURLException ex) {
                Log.e(LOG_TAG, "Malformed URL: " + ex.getMessage(), ex);
            } catch (JSONException ex) {
                Log.e(LOG_TAG, "Malformed JSON: " + ex.getMessage(), ex);
            }
            return null; //Satisfy return
        }

        @Override
        public void onProgressUpdate(MovieAdapter.MovieReference... progress)
        {
            DiscoverFragment.this.mAdapter.addMovie(progress[0]);
        }
    }
}
