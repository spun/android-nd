package com.spundev.bakingtime;

import android.content.ContentValues;
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
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.spundev.bakingtime.adapter.IngredientsAdapter;
import com.spundev.bakingtime.databinding.FragmentRecipeIngredientsBinding;
import com.spundev.bakingtime.model.Ingredient;
import com.spundev.bakingtime.provider.DatabaseContract;
import com.spundev.bakingtime.widget.ShoppingListWidgetProvider;


public class RecipeDetailIngredientsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    // Bundle argument for selected recipe id
    public static final String SELECTED_RECIPE = "SELECTED_RECIPE";
    private int selectedRecipe;

    // Loader id
    private static final int INGREDIENTS_LOADER = 20;

    // Columns projection
    private static final String[] INGREDIENT_COLUMNS = {
            DatabaseContract.IngredientEntry._ID,
            DatabaseContract.IngredientEntry.COLUMN_NAME,
            DatabaseContract.IngredientEntry.COLUMN_QUANTITY,
            DatabaseContract.IngredientEntry.COLUMN_MEASURE,
            DatabaseContract.IngredientEntry.COLUMN_AVAILABLE,
            DatabaseContract.IngredientEntry.COLUMN_RECIPE_ID
    };

    // These indices are tied to INGREDIENT_COLUMNS. If INGREDIENT_COLUMNS changes, these must change.
    public static final int COL_INGREDIENT_ID = 0;
    public static final int COL_INGREDIENT_NAME = 1;
    public static final int COL_INGREDIENT_QUANTITY = 2;
    public static final int COL_INGREDIENT_MEASURE = 3;
    public static final int COL_INGREDIENT_AVAILABLE = 4;
    public static final int COL_INGREDIENT_RECIPE_ID = 5;

    private RecyclerView ingredientsRecyclerView;
    private IngredientsAdapter ingredientsAdapter;
    private TextView emptyIngredientsTextView;

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Data binding
        FragmentRecipeIngredientsBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_recipe_ingredients, container, false);

        // retrieve the id of the selected recipe from the bundle
        Bundle arguments = getArguments();
        if (arguments != null) {
            selectedRecipe = arguments.getInt(SELECTED_RECIPE);
        }

        // RecyclerView
        ingredientsRecyclerView = binding.ingredientsRecyclerView;
        // Better performance on fixed sizes
        ingredientsRecyclerView.setHasFixedSize(true);
        // Divider lines
        ingredientsRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));

        // Empty message
        emptyIngredientsTextView = binding.emptyIngredientsTextView;

        // LinearLayoutManager
        ingredientsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));

        // The adapter will take data from a source and
        // use it to populate the RecyclerView it's attached to.
        ingredientsAdapter = new IngredientsAdapter(getActivity(), new IngredientsAdapter.IngredientsAdapterOnClickHandler() {

            @Override
            public void onCheckedChanged(Ingredient ingredient, boolean isChecked) {

                // change availability value on the database
                ContentValues updatedValues = new ContentValues();
                updatedValues.put(DatabaseContract.IngredientEntry.COLUMN_AVAILABLE, isChecked);
                getActivity().getContentResolver().update(
                        DatabaseContract.IngredientEntry.buildIngredientsFromRecipeUri(ingredient.getRecipeId()),
                        updatedValues,
                        DatabaseContract.IngredientEntry._ID + "= ?",
                        new String[]{Long.toString(ingredient.getId())}
                );

                // Update all widgets
                ShoppingListWidgetProvider.updateShoppingListWidgets(getActivity());
            }
        });
        ingredientsRecyclerView.setAdapter(ingredientsAdapter);

        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(INGREDIENTS_LOADER, null, this);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        if (id == INGREDIENTS_LOADER) {
            // Uri for all the ingredients on an recipe
            Uri savedMoviesUri = DatabaseContract.IngredientEntry.buildIngredientsFromRecipeUri(selectedRecipe);

            // Return cursor loader
            return new CursorLoader(getActivity(),
                    savedMoviesUri,
                    INGREDIENT_COLUMNS,
                    null,
                    null,
                    null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        int loaderId = loader.getId();
        if (loaderId == INGREDIENTS_LOADER) {
            // Send ingredients to the adapter
            ingredientsAdapter.swapCursor(data);

            // Check if we have data to show
            if (data.getCount() == 0) {
                ingredientsRecyclerView.setVisibility(View.GONE);
                emptyIngredientsTextView.setVisibility(View.VISIBLE);
            } else {
                ingredientsRecyclerView.setVisibility(View.VISIBLE);
                emptyIngredientsTextView.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        int loaderId = loader.getId();
        if (loaderId == INGREDIENTS_LOADER) {
            ingredientsAdapter.swapCursor(null);
        }
    }
}
