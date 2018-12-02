package com.spundev.bakingtime.adapter;

import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.spundev.bakingtime.R;
import com.spundev.bakingtime.RecipeDetailStepsFragment;
import com.spundev.bakingtime.databinding.ListItemStepBinding;
import com.spundev.bakingtime.model.Step;

/**
 * Created by spundev
 */

public class StepsAdapter extends RecyclerView.Adapter<StepsAdapter.StepsAdapterViewHolder> {

    private Cursor mCursor;
    final private StepsAdapterOnClickHandler mClickHandler;

    public interface StepsAdapterOnClickHandler {
        void onClick(Step step);
    }

    public StepsAdapter(StepsAdapterOnClickHandler ch) {
        mClickHandler = ch;
    }

    @NonNull
    @Override
    public StepsAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        ListItemStepBinding binding = DataBindingUtil.inflate(layoutInflater, R.layout.list_item_step, parent, false);
        return new StepsAdapterViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull StepsAdapterViewHolder holder, int position) {
        Step step = getValueAt(position);
        holder.bind(step);
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

    private Step getValueAt(int position) {
        mCursor.moveToPosition(position);

        // Get data
        int id = mCursor.getInt(RecipeDetailStepsFragment.COL_STEP_ID);
        String shortDescription = mCursor.getString(RecipeDetailStepsFragment.COL_STEP_SHORT_DESCRIPTION);
        String description = mCursor.getString(RecipeDetailStepsFragment.COL_STEP_DESCRIPTION);
        String videoURL = mCursor.getString(RecipeDetailStepsFragment.COL_STEP_VIDEO_URL);
        String thumbnailURL = mCursor.getString(RecipeDetailStepsFragment.COL_STEP_THUMBNAIL_URL);
        // Create and return step
        return new Step(id, shortDescription, description, videoURL, thumbnailURL);
    }

    public class StepsAdapterViewHolder extends RecyclerView.ViewHolder {
        private final ListItemStepBinding binding;

        StepsAdapterViewHolder(ListItemStepBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Step step) {
            binding.setStep(step);
            binding.setHandlers(new StepsAdapterHandlers());
            binding.executePendingBindings();
        }

        public class StepsAdapterHandlers {
            public void onClickStep(@SuppressWarnings("unused") View view) {
                if (mClickHandler != null) {
                    int adapterPosition = getAdapterPosition();
                    Step m = getValueAt(adapterPosition);
                    mClickHandler.onClick(m);
                }
            }
        }
    }
}