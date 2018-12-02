package com.spundev.popularmovies.adapter;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.spundev.popularmovies.R;
import com.spundev.popularmovies.databinding.ListItemMoviePosterBinding;
import com.spundev.popularmovies.model.TMDBMovie;

import java.util.List;

/**
 * Created by spundev.
 */

public class MoviePostersAdapter extends RecyclerView.Adapter<MoviePostersAdapter.MoviePostersAdapterViewHolder> {

    final private MoviePostersAdapterOnClickHandler mClickHandler;
    private List<TMDBMovie> mValues;

    public interface MoviePostersAdapterOnClickHandler {
        void onClick(TMDBMovie m);
    }


    public MoviePostersAdapter(MoviePostersAdapterOnClickHandler ch) {
        mClickHandler = ch;
        mValues = null;
    }

    @Override
    public MoviePostersAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        ListItemMoviePosterBinding binding = DataBindingUtil.inflate(layoutInflater, R.layout.list_item_movie_poster, parent, false);
        return new MoviePostersAdapterViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(MoviePostersAdapterViewHolder holder, int position) {
        TMDBMovie movie = getValueAt(position);
        holder.bind(movie);
    }

    @Override
    public int getItemCount() {
        if (mValues != null) {
            return mValues.size();
        } else {
            return 0;
        }
    }

    private TMDBMovie getValueAt(int position) {
        return mValues.get(position);
    }

    public void setItems(List<TMDBMovie> items) {
        mValues = items;
        notifyDataSetChanged();
    }

    public class MoviePostersAdapterViewHolder extends RecyclerView.ViewHolder {

        final ListItemMoviePosterBinding binding;

        MoviePostersAdapterViewHolder(ListItemMoviePosterBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(TMDBMovie movie) {
            binding.setMovie(movie);
            binding.setHandlers(new MoviePostersAdapterHandlers());
            binding.executePendingBindings();
        }

        public class MoviePostersAdapterHandlers {
            public void onClickMovie(@SuppressWarnings("unused") View view) {
                if (mClickHandler != null) {
                    int adapterPosition = getAdapterPosition();
                    TMDBMovie m = getValueAt(adapterPosition);
                    mClickHandler.onClick(m);
                }
            }
        }
    }
}
