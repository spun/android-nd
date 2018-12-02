package com.spundev.bakingtime.adapter;

import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.spundev.bakingtime.R;
import com.spundev.bakingtime.databinding.ListItemRecipeWidgetBinding;
import com.spundev.bakingtime.model.Recipe;
import com.spundev.bakingtime.widget.ShoppingListWidgetConfigureActivity;

/**
 * Created by spundev
 */

public class WidgetConfigurationAdapter extends RecyclerView.Adapter<WidgetConfigurationAdapter.RecipesWidgetAdapterViewHolder> {

    private Cursor mCursor;
    final private RecipesWidgetAdapterOnClickHandler mClickHandler;

    public interface RecipesWidgetAdapterOnClickHandler {
        void onClick(Recipe recipe);
    }

    public WidgetConfigurationAdapter(RecipesWidgetAdapterOnClickHandler ch) {
        mClickHandler = ch;
    }

    @NonNull
    @Override
    public RecipesWidgetAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        ListItemRecipeWidgetBinding binding = DataBindingUtil.inflate(layoutInflater, R.layout.list_item_recipe_widget, parent, false);
        return new RecipesWidgetAdapterViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipesWidgetAdapterViewHolder holder, int position) {
        Recipe recipe = getValueAt(position);
        holder.bind(recipe);
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

    private Recipe getValueAt(int position) {
        mCursor.moveToPosition(position);

        // Get data
        int id = mCursor.getInt(ShoppingListWidgetConfigureActivity.COL_RECIPE_ID);
        String name = mCursor.getString(ShoppingListWidgetConfigureActivity.COL_RECIPE_NAME);
        // Create and return recipe
        return new Recipe(id, name, 0, "", 0, 0);
    }

    public class RecipesWidgetAdapterViewHolder extends RecyclerView.ViewHolder {

        private final ListItemRecipeWidgetBinding binding;

        RecipesWidgetAdapterViewHolder(ListItemRecipeWidgetBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Recipe recipe) {
            binding.setRecipe(recipe);
            binding.setHandlers(new RecipesWidgetAdapterHandlers());
            binding.executePendingBindings();
        }

        public class RecipesWidgetAdapterHandlers {
            public void onClickRecipe(@SuppressWarnings("unused") View view) {
                if (mClickHandler != null) {
                    int adapterPosition = getAdapterPosition();
                    Recipe m = getValueAt(adapterPosition);
                    mClickHandler.onClick(m);
                }
            }
        }
    }
}
