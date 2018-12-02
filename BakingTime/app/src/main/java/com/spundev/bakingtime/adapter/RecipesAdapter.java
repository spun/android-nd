package com.spundev.bakingtime.adapter;

import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.spundev.bakingtime.MainActivity;
import com.spundev.bakingtime.R;
import com.spundev.bakingtime.databinding.ListItemRecipeBinding;
import com.spundev.bakingtime.model.Recipe;

/**
 * Created by spundev
 */

public class RecipesAdapter extends RecyclerView.Adapter<RecipesAdapter.RecipesAdapterViewHolder> {

    private Cursor mCursor;
    final private RecipesAdapterOnClickHandler mClickHandler;


    public interface RecipesAdapterOnClickHandler {
        void onClick(Recipe recipe);
    }

    public RecipesAdapter(RecipesAdapterOnClickHandler ch) {
        mClickHandler = ch;
    }

    @NonNull
    @Override
    public RecipesAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        ListItemRecipeBinding binding = DataBindingUtil.inflate(layoutInflater, R.layout.list_item_recipe, parent, false);
        return new RecipesAdapterViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipesAdapterViewHolder holder, int position) {
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
        int id = mCursor.getInt(MainActivity.COL_RECIPE_ID);
        String name = mCursor.getString(MainActivity.COL_RECIPE_NAME);
        int servings = mCursor.getInt(MainActivity.COL_NUM_SERVINGS);
        String image = mCursor.getString(MainActivity.COL_IMAGE_URL);
        int numSteps = mCursor.getInt(MainActivity.COL_NUM_STEPS);
        int numIngredients = mCursor.getInt(MainActivity.COL_NUM_INGREDIENTS);
        // Create and return recipe
        return new Recipe(id, name, servings, image, numSteps, numIngredients);
    }

    public class RecipesAdapterViewHolder extends RecyclerView.ViewHolder {

        private final ListItemRecipeBinding binding;

        RecipesAdapterViewHolder(ListItemRecipeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Recipe recipe) {
            binding.setRecipe(recipe);
            binding.setHandlers(new RecipesAdapterHandlers());
            binding.executePendingBindings();
        }

        public class RecipesAdapterHandlers {
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
