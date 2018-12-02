package com.spundev.popularmovies;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.spundev.popularmovies.adapter.MoviePostersAdapter;
import com.spundev.popularmovies.databinding.FragmentMainActivityPopularBinding;
import com.spundev.popularmovies.loader.MoviesLoader;
import com.spundev.popularmovies.model.TMDBMovie;

import java.util.List;

public class MainActivityPopularFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<TMDBMovie>> {

    // Loader id
    private static final int POPULAR_LOADER = 10;

    private RecyclerView moviesRecyclerView;
    private MoviePostersAdapter mMoviesAdapter;
    private ProgressBar progressBar;
    private TextView emptyTextView;

    public interface Callback {
        void onPopularItemSelected(TMDBMovie movie);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Data binding
        FragmentMainActivityPopularBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_main_activity_popular, container, false);

        moviesRecyclerView = binding.moviesRecyclerView;
        progressBar = binding.fetchingProgressBar;
        emptyTextView = binding.emptyTextView;

        // Better performance on fixed sizes
        binding.moviesRecyclerView.setHasFixedSize(true);
        // LinearLayoutManager
        int numColumns = getActivity().getResources().getInteger(R.integer.num_columns);
        binding.moviesRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), numColumns));

        // The adapter will take data from a source and
        // use it to populate the RecyclerView it's attached to.
        mMoviesAdapter = new MoviePostersAdapter(new MoviePostersAdapter.MoviePostersAdapterOnClickHandler() {
            @Override
            public void onClick(TMDBMovie movie) {
                // Get selected item and start callback
                ((Callback) getActivity()).onPopularItemSelected(movie);
            }
        });
        moviesRecyclerView.setAdapter(mMoviesAdapter);

        return binding.getRoot();
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(POPULAR_LOADER, null, this);
    }


    @Override
    public Loader<List<TMDBMovie>> onCreateLoader(int id, Bundle args) {
        if (id == POPULAR_LOADER) {
            // Show a progress bar while loading
            progressBar.setVisibility(View.VISIBLE);
            moviesRecyclerView.setVisibility(View.GONE);
            emptyTextView.setVisibility(View.GONE);

            return new MoviesLoader(getActivity(), MoviesLoader.SORT_ORDER_POPULAR);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<List<TMDBMovie>> loader, List<TMDBMovie> data) {
        int loaderId = loader.getId();
        if (loaderId == POPULAR_LOADER) {

            // Send movies to the adapter
            mMoviesAdapter.setItems(data);

            // Hide progress bar
            progressBar.setVisibility(View.GONE);
            // Check if we have data to show
            if (data.isEmpty()) {
                moviesRecyclerView.setVisibility(View.GONE);
                emptyTextView.setVisibility(View.VISIBLE);
            } else {
                moviesRecyclerView.setVisibility(View.VISIBLE);
                emptyTextView.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<List<TMDBMovie>> loader) {

    }
}
