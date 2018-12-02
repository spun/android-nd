package com.spundev.popularmovies.adapter;

import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.spundev.popularmovies.MainActivityFavoritesFragment;
import com.spundev.popularmovies.R;
import com.spundev.popularmovies.databinding.ListItemMovieFavoriteBinding;
import com.spundev.popularmovies.model.TMDBMovie;

/**
 * Created by spundev.
 */

public class MovieFavoritesAdapter extends RecyclerView.Adapter<MovieFavoritesAdapter.MovieFavoritesAdapterViewHolder> {

    private Cursor mCursor;
    final private MovieFavoritesAdapterOnClickHandler mClickHandler;

    public interface MovieFavoritesAdapterOnClickHandler {
        // When a movie from favorites is selected
        void onClick(TMDBMovie m);

        // When a movie from favorites is deleted
        void onRemoveFromFavorites(TMDBMovie m);
    }

    public MovieFavoritesAdapter(MovieFavoritesAdapterOnClickHandler ch) {
        mClickHandler = ch;
    }

    @Override
    public MovieFavoritesAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        ListItemMovieFavoriteBinding binding = DataBindingUtil.inflate(layoutInflater, R.layout.list_item_movie_favorite, parent, false);
        return new MovieFavoritesAdapterViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(MovieFavoritesAdapterViewHolder holder, int position) {
        TMDBMovie movie = getValueAt(position);
        holder.bind(movie);
    }

    @Override
    public int getItemCount() {
        if (null == mCursor) return 0;
        return mCursor.getCount();
    }

    public void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
    }

    private TMDBMovie getValueAt(int position) {
        mCursor.moveToPosition(position);

        int id = mCursor.getInt(MainActivityFavoritesFragment.COL_MOVIE_ID);
        String title = mCursor.getString(MainActivityFavoritesFragment.COL_MOVIE_TITLE);
        String releaseDate = mCursor.getString(MainActivityFavoritesFragment.COL_MOVIE_RELEASE_DATE);
        String posterUrl = mCursor.getString(MainActivityFavoritesFragment.COL_MOVIE_POSTER_URL);
        String voteAverage = mCursor.getString(MainActivityFavoritesFragment.COL_MOVIE_VOTE_AVERAGE);
        String overview = mCursor.getString(MainActivityFavoritesFragment.COL_MOVIE_OVERVIEW);

        return new TMDBMovie(id, title, releaseDate, posterUrl, voteAverage, overview);
    }


    public class MovieFavoritesAdapterViewHolder extends RecyclerView.ViewHolder {

        final ListItemMovieFavoriteBinding binding;

        MovieFavoritesAdapterViewHolder(ListItemMovieFavoriteBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(TMDBMovie movie) {
            binding.setMovie(movie);
            binding.setHandlers(new MovieFavoritesAdapterHandlers());
            binding.executePendingBindings();
        }

        public class MovieFavoritesAdapterHandlers {
            public void onClickMovie(@SuppressWarnings("unused") View view) {
                if (mClickHandler != null) {
                    int adapterPosition = getAdapterPosition();
                    TMDBMovie m = getValueAt(adapterPosition);
                    mClickHandler.onClick(m);
                }
            }

            public void onClickRemoveMovie(@SuppressWarnings("unused") View view) {
                if (mClickHandler != null) {
                    int adapterPosition = getAdapterPosition();
                    TMDBMovie m = getValueAt(adapterPosition);
                    mClickHandler.onRemoveFromFavorites(m);
                }
            }
        }
    }
}
