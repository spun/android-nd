package com.spundev.bakingtime.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.spundev.bakingtime.R;
import com.spundev.bakingtime.RecipeDetailIngredientsFragment;
import com.spundev.bakingtime.databinding.ListItemIngredientBinding;
import com.spundev.bakingtime.databinding.ListItemIngredientTutorialBinding;
import com.spundev.bakingtime.model.Ingredient;

/**
 * Created by spundev
 */

public class IngredientsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // Preferences to hide the "info" card at the top of the list.
    private static final String PREFS_NAME = "com.spundev.bakingtime.adapter.IngredientsAdapter";
    private static final String PREFS_SHOW_INFO_CARD = "PREFS_SHOW_INFO_CARD";
    private boolean showInfoCardHeader;
    // Two view types
    private final static int INFO_CARD_HEADER_VIEW = 0;
    private final static int CONTENT_VIEW = 1;

    private final Context mContext;
    private Cursor mCursor;
    final private IngredientsAdapterOnClickHandler mClickHandler;

    public interface IngredientsAdapterOnClickHandler {
        void onCheckedChanged(Ingredient ingredient, boolean isChecked);
    }

    public IngredientsAdapter(Context context, IngredientsAdapterOnClickHandler ch) {
        mContext = context;
        mClickHandler = ch;
        // Get if the info cards was dismissed
        showInfoCardHeader = getInfoCartStatusPref(mContext);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // We inflate a different layout for the header card and a list item
        if (viewType == INFO_CARD_HEADER_VIEW) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            ListItemIngredientTutorialBinding binding = DataBindingUtil.inflate(layoutInflater, R.layout.list_item_ingredient_tutorial, parent, false);
            return new IngredientsTutorialViewHolder(binding);
        } else if (viewType == CONTENT_VIEW) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            ListItemIngredientBinding binding = DataBindingUtil.inflate(layoutInflater, R.layout.list_item_ingredient, parent, false);
            return new IngredientsAdapterViewHolder(binding);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        int viewType = holder.getItemViewType();
        if (viewType == INFO_CARD_HEADER_VIEW) {
            // Nothing add
            IngredientsTutorialViewHolder viewHolder = (IngredientsTutorialViewHolder) holder;
            viewHolder.bind();
        } else if (viewType == CONTENT_VIEW) {
            // Retrieve ingredient at the position
            Ingredient ingredient = getValueAt(position);
            IngredientsAdapterViewHolder viewHolder = (IngredientsAdapterViewHolder) holder;
            viewHolder.bind(ingredient);
        }
    }

    @Override
    public int getItemCount() {
        if (null != mCursor) {
            // If the card has not dismissed
            if (showInfoCardHeader) {
                // We have to show the card + list items
                return mCursor.getCount() + 1;
            } else { // If the card has dismissed at some point
                // We only have to show the list items
                return mCursor.getCount();
            }
        } else {
            return 0;
        }
    }

    @Override
    public int getItemViewType(int position) {
        // If the card has not dismissed
        if (showInfoCardHeader) {
            // Show items and all positions but the first one
            if (position > 0) {
                return CONTENT_VIEW;
            }
            return INFO_CARD_HEADER_VIEW;
        } else { // If the card has dismissed at some point
            // We only show list items
            return CONTENT_VIEW;
        }
    }


    public void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
    }

    private Ingredient getValueAt(int position) {
        // Get ingredient at position

        // Move to position, checking if we are showing the info card
        if (showInfoCardHeader) {
            mCursor.moveToPosition(position - 1);
        } else {
            mCursor.moveToPosition(position);
        }

        // Get data
        int id = mCursor.getInt(RecipeDetailIngredientsFragment.COL_INGREDIENT_ID);
        String name = mCursor.getString(RecipeDetailIngredientsFragment.COL_INGREDIENT_NAME);
        float quantity = mCursor.getFloat(RecipeDetailIngredientsFragment.COL_INGREDIENT_QUANTITY);
        String measure = mCursor.getString(RecipeDetailIngredientsFragment.COL_INGREDIENT_MEASURE);
        boolean available = mCursor.getInt(RecipeDetailIngredientsFragment.COL_INGREDIENT_AVAILABLE) != 0;
        int recipeId = mCursor.getInt(RecipeDetailIngredientsFragment.COL_INGREDIENT_RECIPE_ID);
        // Create and return ingredient
        return new Ingredient(id, name, quantity, measure, available, recipeId);
    }

    // Helper method to check if the info was dismissed before
    private static boolean getInfoCartStatusPref(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        return prefs.getBoolean(PREFS_SHOW_INFO_CARD, true);
    }

    // Helper method to saved the dismissed info card for the future
    private static void closeInfoCartStatusPref(Context context) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putBoolean(PREFS_SHOW_INFO_CARD, false);
        prefs.apply();
    }


    // INFO_CARD_HEADER_VIEW
    public class IngredientsTutorialViewHolder extends RecyclerView.ViewHolder {

        private final ListItemIngredientTutorialBinding binding;

        IngredientsTutorialViewHolder(ListItemIngredientTutorialBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind() {
            binding.setHandlers(new IngredientsTutorialHandlers());
            binding.executePendingBindings();
        }

        public class IngredientsTutorialHandlers {
            public void onClickClose(@SuppressWarnings("unused") View view) {
                showInfoCardHeader = false;
                closeInfoCartStatusPref(mContext);
                notifyDataSetChanged();

            }
        }
    }

    // CONTENT_VIEW
    public class IngredientsAdapterViewHolder extends RecyclerView.ViewHolder {
        private final ListItemIngredientBinding binding;

        IngredientsAdapterViewHolder(ListItemIngredientBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Ingredient ingredient) {
            binding.setIngredient(ingredient);
            binding.setHandlers(new IngredientsAdapterHandlers());
            binding.executePendingBindings();
        }

        public class IngredientsAdapterHandlers {

            public void onCheckAvailability(@SuppressWarnings("unused") View buttonView) {
                if (mClickHandler != null) {
                    int adapterPosition = getAdapterPosition();
                    Ingredient m = getValueAt(adapterPosition);
                    mClickHandler.onCheckedChanged(m, !m.isAvailable());
                }
            }
        }
    }
}