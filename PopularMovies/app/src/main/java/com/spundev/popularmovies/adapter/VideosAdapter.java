package com.spundev.popularmovies.adapter;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.spundev.popularmovies.R;
import com.spundev.popularmovies.databinding.ListItemInfoMovieVideoBinding;
import com.spundev.popularmovies.model.TMDBVideo;

import java.util.List;

/**
 * Created by spundev.
 */

public class VideosAdapter extends RecyclerView.Adapter<VideosAdapter.VideosAdapterViewHolder> {

    final private VideoAdapterOnClickHandler mClickHandler;
    private List<TMDBVideo> mValues;

    public interface VideoAdapterOnClickHandler {
        void onClick(String m);
    }

    public VideosAdapter(VideoAdapterOnClickHandler ch) {
        mClickHandler = ch;
        mValues = null;
    }

    @Override
    public VideosAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        ListItemInfoMovieVideoBinding binding = DataBindingUtil.inflate(layoutInflater, R.layout.list_item_info_movie_video, parent, false);
        return new VideosAdapterViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(VideosAdapterViewHolder holder, int position) {
        TMDBVideo video = getValueAt(position);
        holder.bind(video);
    }

    @Override
    public int getItemCount() {
        if (mValues != null) {
            return mValues.size();
        } else {
            return 0;
        }
    }

    private TMDBVideo getValueAt(int position) {
        return mValues.get(position);
    }

    public void setItems(List<TMDBVideo> items) {
        mValues = items;
        notifyDataSetChanged();
    }

    public class VideosAdapterViewHolder extends RecyclerView.ViewHolder {

        private final ListItemInfoMovieVideoBinding binding;

        public VideosAdapterViewHolder(ListItemInfoMovieVideoBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(TMDBVideo video) {
            binding.setVideo(video);
            binding.setHandlers(new VideosAdapterHandlers());
            binding.executePendingBindings();
        }

        public class VideosAdapterHandlers {
            public void onClickTrailer(@SuppressWarnings("unused") View view) {
                if (mClickHandler != null) {
                    int adapterPosition = getAdapterPosition();
                    TMDBVideo m = getValueAt(adapterPosition);
                    mClickHandler.onClick(m.getKey());
                }
            }
        }
    }
}
