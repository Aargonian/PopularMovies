package com.example.android.popularmovies;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.example.android.popularmovies.data.MovieContract;
import com.example.android.popularmovies.data.MovieUtil;

import butterknife.Bind;
import butterknife.ButterKnife;


/**
 * Created by Aaron Helton on 1/30/2016
 */
public class DiscoverFragment extends Fragment
        implements MovieAdapter.MovieItemClickListener, LoaderManager.LoaderCallbacks<Cursor>
{
    private static final String LOG_TAG = DiscoverFragment.class.getSimpleName();
    private static final Integer PREFERRED_COLUMN_WIDTH = 100;
    private static final Integer MOVIE_LOADER = 0;

    private OnMovieSelectedListener mListener;
    private RecyclerView mMoviesGridView;
    private MovieAdapter mAdapter;
    private String sort;
    private int currentPage;

    @Bind(R.id.movieContainer) FrameLayout movieContainer;

    public DiscoverFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Log.e(LOG_TAG, "onCreate Called!");
        mAdapter = new MovieAdapter();
        mAdapter.setMovieItemClickedListener(this);
        currentPage = 1;
        sort = Utility.getSort(getActivity());
    }

    @Override
    public void onActivityCreated(Bundle state)
    {
        super.onActivityCreated(state);
        getLoaderManager().initLoader(MOVIE_LOADER, null, this);
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
        ButterKnife.bind(this, root);
        //Create Recyclerview Programmatically so that we can get auto-fit-like functionality
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
        GridLayoutManager mManager = new GridLayoutManager(this.getContext(), 1);
        mMoviesGridView.setLayoutManager(mManager);
        mMoviesGridView.setAdapter(mAdapter);
        movieContainer.addView(mMoviesGridView);
        updateMovies();
        if(savedInstanceState != null)
            mMoviesGridView.setVerticalScrollbarPosition(savedInstanceState.getInt("SCROLL_POS"));
        return root;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if(!sort.equals(Utility.getSort(getActivity()))) {
            getLoaderManager().restartLoader(MOVIE_LOADER, null, this);
            currentPage = 1;
            updateMovies();
        }
    }

    @Override
    public void movieClicked(Long id)
    {
        mListener.onMovieSelected(id);
    }

    public void updateMovies() {
        DiscoverTask task = new DiscoverTask(this.getActivity());
        task.execute(currentPage++);
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

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String sort = Utility.getSort(getActivity());
        if(sort.equalsIgnoreCase(getString(R.string.pref_sort_popularity_value)))
            sort = MovieContract.MovieEntry.COLUMN_POPULARITY + " DESC";
        else if (sort.equalsIgnoreCase(getString(R.string.pref_sort_rating_value)))
            sort = MovieContract.MovieEntry.COLUMN_RATING + " DESC";

        return new CursorLoader(getActivity(), MovieContract.MovieEntry.CONTENT_URI,
                                MovieUtil.movieProjection, null, null, sort);
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.setDataset(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.setDataset(null);
    }

    public interface OnMovieSelectedListener {
        void onMovieSelected(Long id);
    }
}
