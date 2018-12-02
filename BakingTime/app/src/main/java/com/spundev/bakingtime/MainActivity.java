package com.spundev.bakingtime;

import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.spundev.bakingtime.adapter.RecipesAdapter;
import com.spundev.bakingtime.databinding.ActivityMainBinding;
import com.spundev.bakingtime.model.Recipe;
import com.spundev.bakingtime.provider.DatabaseContract;
import com.spundev.bakingtime.sync.RecipesSyncUtils;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    // Loader id
    private static final int RECIPES_LOADER = 10;
    private final String SEARCH_QUERY = "SEARCH_QUERY";

    // Columns projection
    private static final String[] RECIPE_COLUMNS = {
            DatabaseContract.RecipeEntry._ID,
            DatabaseContract.RecipeEntry.COLUMN_NAME,
            DatabaseContract.RecipeEntry.COLUMN_NUM_SERVINGS,
            DatabaseContract.RecipeEntry.COLUMN_IMAGE_URL,
            DatabaseContract.RecipeEntry.COLUMN_NUM_STEPS,
            DatabaseContract.RecipeEntry.COLUMN_NUM_INGREDIENTS
    };

    // These indices are tied to RECIPE_COLUMNS. If RECIPE_COLUMNS changes, these must change.
    public static final int COL_RECIPE_ID = 0;
    public static final int COL_RECIPE_NAME = 1;
    public static final int COL_NUM_SERVINGS = 2;
    public static final int COL_IMAGE_URL = 3;
    public static final int COL_NUM_STEPS = 4;
    public static final int COL_NUM_INGREDIENTS = 5;

    private RecyclerView recipesRecyclerView;
    private RecipesAdapter recipesAdapter;
    private View emptyRecipesView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Data binding
        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        // Toolbar
        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);

        // RecyclerView
        recipesRecyclerView = binding.contentMain.recipesRecyclerView;
        recipesRecyclerView.setHasFixedSize(true);
        // Empty message
        emptyRecipesView = binding.contentMain.emptyRecipesView;
        // Force recipes check button
        binding.contentMain.emptyRecipesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start sync job
                RecipesSyncUtils.startImmediateSync(MainActivity.this);
            }
        });

        // LayoutManager
        int numColumns = getResources().getInteger(R.integer.num_columns);
        recipesRecyclerView.setLayoutManager(new GridLayoutManager(this, numColumns));

        // The adapter will take data from a source and
        // use it to populate the RecyclerView it's attached to.
        recipesAdapter = new RecipesAdapter(new RecipesAdapter.RecipesAdapterOnClickHandler() {
            @Override
            public void onClick(Recipe recipe) {
                // Launch recipe detail activity
                Intent intent = new Intent(MainActivity.this, RecipeDetailActivity.class);
                intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipe.getId());
                startActivity(intent);
            }
        });
        recipesRecyclerView.setAdapter(recipesAdapter);

        // Recipes loader
        getSupportLoaderManager().initLoader(RECIPES_LOADER, null, this);

        // Setup sync job
        RecipesSyncUtils.initialize(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        // Search action
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        // Search events
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // We get the text an restart the loader with the query
                Bundle searchArgs = new Bundle();
                searchArgs.putString(SEARCH_QUERY, query);
                getSupportLoaderManager().restartLoader(RECIPES_LOADER, searchArgs, MainActivity.this);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // We get the text an restart the loader with the query
                Bundle searchArgs = new Bundle();
                searchArgs.putString(SEARCH_QUERY, newText);
                getSupportLoaderManager().restartLoader(RECIPES_LOADER, searchArgs, MainActivity.this);
                return false;
            }
        });
        return true;
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {

        if (id == RECIPES_LOADER) {
            // All recipes Uri
            Uri savedMoviesUri = DatabaseContract.RecipeEntry.CONTENT_URI;

            // Check if we have a search query (menu action)
            String selection = null;
            String[] selectionArgs = null;
            if (args != null) {
                String searchQuery = args.getString(SEARCH_QUERY);
                if (searchQuery != null && !searchQuery.isEmpty()) {
                    selection = DatabaseContract.RecipeEntry.COLUMN_NAME + " LIKE ?";
                    selectionArgs = new String[]{"%" + searchQuery + "%"};
                }
            }
            // Return cursor loader
            return new CursorLoader(this,
                    savedMoviesUri,
                    RECIPE_COLUMNS,
                    selection,
                    selectionArgs,
                    null);
        }
        return null;
    }


    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {

        int loaderId = loader.getId();
        if (loaderId == RECIPES_LOADER) {
            // Send recipes to the adapter
            recipesAdapter.swapCursor(data);

            // Check if we have data to show
            if (data.getCount() == 0) {
                recipesRecyclerView.setVisibility(View.GONE);
                emptyRecipesView.setVisibility(View.VISIBLE);
            } else {
                recipesRecyclerView.setVisibility(View.VISIBLE);
                emptyRecipesView.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        int loaderId = loader.getId();
        if (loaderId == RECIPES_LOADER) {
            recipesAdapter.swapCursor(null);
        }
    }
}
