package com.example.android.popularmovies;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import java.io.InputStream;
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
    private FrameLayout movieContainer;
    private String prevSort;
    private boolean loadingMovies;
    private int currentPage;

    public DiscoverFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Log.e(LOG_TAG, "onCreate Called!");
        mAdapter = new MovieAdapter();
        mAdapter.setMovieItemClickedListener(this);
        currentPage = 1;
    }

    public void onSaveInstanceState(Bundle bundle)
    {
        super.onSaveInstanceState(bundle);
        int scrollPosition =
                ((GridLayoutManager)mMoviesGridView.getLayoutManager())
                        .findFirstVisibleItemPosition();
        bundle.putInt("SCROLL_POS", scrollPosition);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_discover, container, false);
        movieContainer = (FrameLayout)root.findViewById(R.id.movieContainer);
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
        mMoviesGridView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (currentPage < 11) //check for scroll down
                {
                    int visibleItemCount = recyclerView.getLayoutManager().getChildCount();
                    int totalItemCount = recyclerView.getLayoutManager().getItemCount();
                    int pastVisiblesItems =
                            ((GridLayoutManager) recyclerView.getLayoutManager())
                                    .findFirstVisibleItemPosition();

                    if (!loadingMovies) {
                        if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                            addMovies();
                        }
                    }
                }
            }
        });
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
        addMovies();
        if(savedInstanceState != null)
            mMoviesGridView.setVerticalScrollbarPosition(savedInstanceState.getInt("SCROLL_POS"));
        return root;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getContext());
        if(!prefs.getString(getString(R.string.pref_sort_key), getString(R.string.pref_sort_default))
                .equalsIgnoreCase(prevSort))
        {
            mAdapter.emptyDataset();
            currentPage=1;
            addMovies();
        }
    }

    @Override
    public void movieClicked(Integer id, String title)
    {
        mListener.onMovieSelected(id, title);
    }

    public void addMovies() {
        if(currentPage < 11)
        {
            loadingMovies = true;
            DiscoverTask task = new DiscoverTask() {
                @Override
                public void onPostExecute(Void result) {
                    loadingMovies = false;
                }
            };
            task.execute(currentPage++);
        }

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

    private class DiscoverTask extends AsyncTask<Integer, MovieAdapter.MovieReference, Void>
    {
        private final String LOG_TAG = DiscoverTask.class.getSimpleName();

        @Override
        public Void doInBackground(Integer... params)
        {
            int page = 1;
            if(params.length > 0)
                page = params[0];
            String TMDB_DISCOVER_URL = Movie.TMDB_URL_BASE+"/discover/movie?";
            Context context = DiscoverFragment.this.getActivity();
            SharedPreferences prefs =
                    PreferenceManager
                            .getDefaultSharedPreferences(DiscoverFragment.this.getActivity());
            String SORT = prefs.getString(getString(R.string.pref_sort_key), getString(R.string.pref_sort_default));
            prevSort = SORT;
            if(SORT.equalsIgnoreCase(getString(R.string.pref_sort_popularity_value)))
                SORT = "popularity.desc";
            else if (SORT.equalsIgnoreCase(getString(R.string.pref_sort_rating_value)))
                SORT = "vote_average.desc";
            else
                SORT = "release_date.desc";
            try {
                Uri.Builder builder = Uri.parse(TMDB_DISCOVER_URL).buildUpon()
                        .appendQueryParameter("sort_by", SORT)
                        .appendQueryParameter("api_key", Movie.API_KEY)
                        .appendQueryParameter("page", Integer.toString(page));
                //Hack to avoid strange results in release date or vote average sorts
                if(SORT.equals("release_date.desc") || SORT.equals("vote_average.desc"))
                    builder.appendQueryParameter("vote_count.gte", "250");
                Uri uri = builder.build();
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
                        Log.e(LOG_TAG, "Unable to retrieve poster! " + ex.getMessage(), ex);
                    }

                    //Retrieve default no image bitmap if no image exists for the movie
                    if(poster == null) {
                        InputStream stream = null;
                        try {
                            // get input stream
                            stream = getResources().getAssets().open("noimage.png");
                            poster = BitmapFactory.decodeStream(stream);
                        } catch(IOException ex) {
                            Log.e(LOG_TAG, "UNABLE TO DECODE DEFAULT IMAGE: "+ex.getMessage(), ex);
                            poster = null;
                        } finally {
                            if(stream != null) {
                                try {
                                    stream.close();
                                } catch (IOException e) {
                                    Log.e(LOG_TAG, "Unable to close stream: " + e.getMessage(), e);
                                }
                            }
                        }
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
