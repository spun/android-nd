package com.spundev.popularmovies.adapter;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.spundev.popularmovies.R;
import com.spundev.popularmovies.databinding.ListItemInfoMovieReviewBinding;
import com.spundev.popularmovies.model.TMDBReview;

import java.util.List;

/**
 * Created by spundev.
 */

public class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.ReviewsAdapterViewHolder> {

    private List<TMDBReview> mValues;

    public ReviewsAdapter() {
        mValues = null;
    }

    @Override
    public ReviewsAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        ListItemInfoMovieReviewBinding binding = DataBindingUtil.inflate(layoutInflater, R.layout.list_item_info_movie_review, parent, false);
        return new ReviewsAdapterViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(ReviewsAdapterViewHolder holder, final int position) {

        final TMDBReview review = getValueAt(position);
        holder.bind(review);

        // Expand/contract long comments on click
        final boolean isExpanded = review.isExpanded();
        if (isExpanded) {
            holder.binding.reviewContentTextView.setSingleLine(false);
            holder.binding.reviewContentTextView.setEllipsize(null);
        } else {
            holder.binding.reviewContentTextView.setLines(4);
            holder.binding.reviewContentTextView.setEllipsize(TextUtils.TruncateAt.END);
        }
        holder.itemView.setActivated(isExpanded);
    }

    @Override
    public int getItemCount() {
        if (mValues != null) {
            return mValues.size();
        } else {
            return 0;
        }
    }

    private TMDBReview getValueAt(int position) {
        return mValues.get(position);
    }

    public void setItems(List<TMDBReview> items) {
        mValues = items;
        notifyDataSetChanged();
    }


    public class ReviewsAdapterViewHolder extends RecyclerView.ViewHolder {

        private final ListItemInfoMovieReviewBinding binding;

        public ReviewsAdapterViewHolder(ListItemInfoMovieReviewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(TMDBReview review) {
            binding.setReview(review);
            binding.setHandlers(new ReviewsAdapterHandlers());
            binding.executePendingBindings();
        }

        public class ReviewsAdapterHandlers {
            // Expand/contract long comments on click
            public void onClickReview(@SuppressWarnings("unused") View view) {
                int adapterPosition = getAdapterPosition();
                TMDBReview r = getValueAt(adapterPosition);
                r.setExpanded(!r.isExpanded());
                notifyDataSetChanged();
            }
        }
    }
}
