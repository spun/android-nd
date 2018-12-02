package com.spundev.popularmovies;

import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.spundev.popularmovies.adapter.MovieFavoritesAdapter;
import com.spundev.popularmovies.data.DatabaseContract;
import com.spundev.popularmovies.databinding.FragmentMainActivityFavoritesBinding;
import com.spundev.popularmovies.model.TMDBMovie;

public class MainActivityFavoritesFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    // Loader id
    private static final int FAVORITES_LOADER = 30;

    // Columns projection
    private static final String[] MOVIE_COLUMNS = {
            DatabaseContract.MovieEntry._ID,
            DatabaseContract.MovieEntry.COLUMN_TITLE,
            DatabaseContract.MovieEntry.COLUMN_RELEASE_DATE,
            DatabaseContract.MovieEntry.COLUMN_POSTER_URL,
            DatabaseContract.MovieEntry.COLUMN_VOTE_AVERAGE,
            DatabaseContract.MovieEntry.COLUMN_OVERVIEW
    };

    // These indices are tied to MOVIE_COLUMNS. If MOVIE_COLUMNS changes, these must change.
    public static final int COL_MOVIE_ID = 0;
    public static final int COL_MOVIE_TITLE = 1;
    public static final int COL_MOVIE_RELEASE_DATE = 2;
    public static final int COL_MOVIE_POSTER_URL = 3;
    public static final int COL_MOVIE_VOTE_AVERAGE = 4;
    public static final int COL_MOVIE_OVERVIEW = 5;

    private RecyclerView moviesRecyclerView;
    private MovieFavoritesAdapter mMoviesAdapter;
    private TextView emptyTextView;

    public interface Callback {
        void onFavoriteItemSelected(TMDBMovie movie);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Data binding
        FragmentMainActivityFavoritesBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_main_activity_favorites, container, false);

        moviesRecyclerView = binding.moviesRecyclerView;
        emptyTextView = binding.emptyTextView;

        // Better performance on fixed sizes
        moviesRecyclerView.setHasFixedSize(true);
        // LinearLayoutManager
        moviesRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));

        // The adapter will take data from a source and
        // use it to populate the RecyclerView it's attached to.
        mMoviesAdapter = new MovieFavoritesAdapter(new MovieFavoritesAdapter.MovieFavoritesAdapterOnClickHandler() {
            @Override
            public void onClick(TMDBMovie movie) {
                // Get selected item and start callback
                ((Callback) getActivity()).onFavoriteItemSelected(movie);
            }

            @Override
            public void onRemoveFromFavorites(TMDBMovie m) {
                // delete from favorite list
                getActivity().getContentResolver().delete(DatabaseContract.MovieEntry.CONTENT_URI,
                        DatabaseContract.MovieEntry._ID + " = ?", new String[]{Integer.toString(m.getId())});

                Toast.makeText(getActivity(), R.string.remove_from_favorites_list, Toast.LENGTH_SHORT).show();
            }
        });
        moviesRecyclerView.setAdapter(mMoviesAdapter);

        return binding.getRoot();
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(FAVORITES_LOADER, null, this);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        if (id == FAVORITES_LOADER) {
            // This is called when a new Loader needs to be created.
            Uri savedMoviesUri = DatabaseContract.MovieEntry.CONTENT_URI;

            return new CursorLoader(getActivity(),
                    savedMoviesUri,
                    MOVIE_COLUMNS,
                    null,
                    null,
                    DatabaseContract.MovieEntry.COLUMN_RELEASE_DATE);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        int loaderId = loader.getId();
        if (loaderId == FAVORITES_LOADER) {
            mMoviesAdapter.swapCursor(data);

            // Check if we have data to show
            if (data.getCount() == 0) {
                moviesRecyclerView.setVisibility(View.GONE);
                emptyTextView.setVisibility(View.VISIBLE);
            } else {
                moviesRecyclerView.setVisibility(View.VISIBLE);
                emptyTextView.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        int loaderId = loader.getId();
        if (loaderId == FAVORITES_LOADER) {
            mMoviesAdapter.swapCursor(null);
        }
    }
}
