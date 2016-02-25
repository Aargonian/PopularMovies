package com.example.android.popularmovies;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import butterknife.Bind;
import butterknife.ButterKnife;


/**
 * Created by Aaron Helton on 1/30/2016
 */
public class DiscoverFragment extends Fragment implements MovieAdapter.MovieItemClickListener
{
    //Neglecting use of ButterKnife for this fragment, only one findViewById() is used.
    private static final String LOG_TAG = DiscoverFragment.class.getSimpleName();
    private static final Integer PREFERRED_COLUMN_WIDTH = 100;
    private static final Integer MOVIE_LOADER = 0;
    private OnMovieSelectedListener mListener;
    private RecyclerView mMoviesGridView;
    private MovieAdapter mAdapter;

    private String sort;
    private boolean loadingMovies;
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
        mMoviesGridView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
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
        });
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
        addMovies();
        if(savedInstanceState != null)
            mMoviesGridView.setVerticalScrollbarPosition(savedInstanceState.getInt("SCROLL_POS"));
        return root;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if(!sort.equals(Utility.getSort(getActivity()))) {
            mAdapter.emptyDataset();
            currentPage = 1;
            addMovies();
        }
    }

    @Override
    public void movieClicked(Long id)
    {
        mListener.onMovieSelected(id);
    }

    public void addMovies() {
        if(currentPage < 11)
        {
            loadingMovies = true;
            DiscoverTask task = new DiscoverTask(this.getActivity()) {
                @Override
                public void onProgressUpdate(String... update)
                {
                    if(update.length == 2)
                    {
                        mAdapter.addMovie(Long.parseLong(update[0]), update[1]);
                    }
                }
                @Override
                public void onPostExecute(Void result) {
                    loadingMovies = false;
                }
            };
            Log.d(LOG_TAG, "CURRENT PAGE: " + currentPage);
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
        void onMovieSelected(Long id);
    }
}
