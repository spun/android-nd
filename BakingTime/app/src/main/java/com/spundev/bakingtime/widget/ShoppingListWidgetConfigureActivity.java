package com.spundev.bakingtime.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.spundev.bakingtime.R;
import com.spundev.bakingtime.adapter.WidgetConfigurationAdapter;
import com.spundev.bakingtime.databinding.ShoppingListWidgetConfigureBinding;
import com.spundev.bakingtime.model.Recipe;
import com.spundev.bakingtime.provider.DatabaseContract;

/**
 * The configuration screen for the {@link ShoppingListWidgetProvider ShoppingListWidgetProvider} AppWidget.
 */
public class ShoppingListWidgetConfigureActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String PREFS_NAME = "com.spundev.bakingtime.widget.ShoppingListWidgetProvider";
    private static final String PREF_PREFIX_KEY = "appwidget_";
    public static final String EXTRA_APPWIDGET_UPDATE_MODE = "EXTRA_APPWIDGET_UPDATE_MODE";
    private boolean isUpdate = false;
    private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    // Loader id
    private static final int RECIPES_LOADER = 10;

    // Columns projection
    private static final String[] RECIPE_COLUMNS = {
            DatabaseContract.RecipeEntry._ID,
            DatabaseContract.RecipeEntry.COLUMN_NAME
    };

    // These indices are tied to RECIPE_COLUMNS. If RECIPE_COLUMNS changes, these must change.
    public static final int COL_RECIPE_ID = 0;
    public static final int COL_RECIPE_NAME = 1;

    private RecyclerView recipesRecyclerView;
    private WidgetConfigurationAdapter recipesAdapter;
    private TextView emptyTextView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED);

        // Data binding
        ShoppingListWidgetConfigureBinding binding = DataBindingUtil.setContentView(this, R.layout.shopping_list_widget_configure);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Find the widget id from the intent.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            isUpdate = extras.getBoolean(EXTRA_APPWIDGET_UPDATE_MODE, false);
        }
        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

        // RecyclerView
        recipesRecyclerView = binding.recipesRecyclerView;
        recipesRecyclerView.setHasFixedSize(true);
        recipesRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        // Empty message
        emptyTextView = binding.emptyTextView;

        // We use a Linear layout manager
        recipesRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // The adapter will take data from a source and
        // use it to populate the RecyclerView it's attached to.
        recipesAdapter = new WidgetConfigurationAdapter(new WidgetConfigurationAdapter.RecipesWidgetAdapterOnClickHandler() {
            @Override
            public void onClick(Recipe recipe) {
                // Problems when using the activity context ¿?
                final Context context = getApplicationContext();

                // store the selection locally
                saveWidgetPref(context, mAppWidgetId, recipe.getId());

                // It is the responsibility of the configuration activity to update the app widget
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

                // Create/update widget
                ShoppingListWidgetProvider.updateAppWidget(context, appWidgetManager, mAppWidgetId);
                // If the widget already exists and we were changing the recipe
                if (isUpdate) {
                    appWidgetManager.notifyAppWidgetViewDataChanged(mAppWidgetId, R.id.shopping_list);
                }

                // Make sure we pass back the original appWidgetId
                Intent resultValue = new Intent();
                resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
                setResult(RESULT_OK, resultValue);
                finish();
            }
        });
        recipesRecyclerView.setAdapter(recipesAdapter);

        getSupportLoaderManager().initLoader(RECIPES_LOADER, null, this);
    }

    // Write the prefix to the SharedPreferences object for this widget
    private static void saveWidgetPref(Context context, int appWidgetId, int recipeId) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putInt(PREF_PREFIX_KEY + appWidgetId, recipeId);
        prefs.apply();
    }

    public static int loadWidgetPref(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        return prefs.getInt(PREF_PREFIX_KEY + appWidgetId, -1);
    }

    public static void deleteWidgetPref(Context context, int appWidgetId) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.remove(PREF_PREFIX_KEY + appWidgetId);
        prefs.apply();
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        if (id == RECIPES_LOADER) {
            // Uri for all the recipes
            Uri recipesUri = DatabaseContract.RecipeEntry.CONTENT_URI;

            // Return cursor loader
            return new CursorLoader(this,
                    recipesUri,
                    RECIPE_COLUMNS,
                    null,
                    null,
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
                emptyTextView.setVisibility(View.VISIBLE);
            } else {
                recipesRecyclerView.setVisibility(View.VISIBLE);
                emptyTextView.setVisibility(View.GONE);
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

