package com.spundev.bakingtime;

import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.spundev.bakingtime.adapter.DetailsPagerAdapter;
import com.spundev.bakingtime.databinding.ActivityRecipeDetailBinding;
import com.spundev.bakingtime.model.Step;
import com.spundev.bakingtime.provider.DatabaseContract;

public class RecipeDetailActivity extends AppCompatActivity implements RecipeDetailStepsFragment.Callback, LoaderManager.LoaderCallbacks<Cursor> {

    // Layout
    private boolean mTwoPane;

    // Intent extra for selected recipe id
    public static final String EXTRA_RECIPE_ID = "EXTRA_RECIPE_ID";
    private int selectedRecipeId;

    // Loader id
    private static final int RECIPE_LOADER = 10;

    // Columns projection
    private static final String[] RECIPE_COLUMNS = {
            DatabaseContract.RecipeEntry.COLUMN_NAME,
            DatabaseContract.RecipeEntry.COLUMN_IMAGE_URL
    };
    // These indices are tied to RECIPE_COLUMNS. If RECIPE_COLUMNS changes, these must change.
    private static final int COL_RECIPE_NAME = 0;
    private static final int COL_IMAGE_URL = 1;

    // Detail fragment tag
    private static final String STEP_DETAIL_FRAGMENT_TAG = "STEP_DETAIL_FRAGMENT_TAG";

    // Data binding
    private ActivityRecipeDetailBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Data binding
        binding = DataBindingUtil.setContentView(this, R.layout.activity_recipe_detail);

        // Toolbar
        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        // Toolbar back arrow and title
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setTitle("");
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }

        // retrieve the id of the selected recipe from the intent
        Intent intent = getIntent();
        selectedRecipeId = intent.getIntExtra(EXTRA_RECIPE_ID, 0);

        // Recipes and ingredients view pager
        DetailsPagerAdapter adapter = new DetailsPagerAdapter(selectedRecipeId, getSupportFragmentManager());
        ViewPager viewPager = binding.myViewPager;
        viewPager.setAdapter(adapter);

        // TabLayout
        TabLayout tabLayout = binding.detailTabs;
        tabLayout.setupWithViewPager(viewPager);

        // twoPane check
        if (findViewById(R.id.detail_container) != null) {
            // The detail container view will be present only in the large-screen layouts
            // (res/layout-sw600dp). If this view is present, then the activity should be
            // in two-pane mode.
            mTwoPane = true;
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            if (savedInstanceState == null) {
                Bundle args = new Bundle();
                args.putInt(StepDetailActivityFragment.SELECTED_RECIPE_EXTRA, selectedRecipeId);
                StepDetailActivityFragment fragment = new StepDetailActivityFragment();
                fragment.setArguments(args);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.detail_container, fragment, STEP_DETAIL_FRAGMENT_TAG)
                        .commit();
            }
        } else {
            mTwoPane = false;
        }

        // Recipe loader
        getSupportLoaderManager().initLoader(RECIPE_LOADER, null, this);
    }

    @Override
    public void onStepItemSelected(Step step) {

        Bundle args = new Bundle();
        args.putInt(StepDetailActivityFragment.SELECTED_RECIPE_EXTRA, selectedRecipeId);
        args.putInt(StepDetailActivityFragment.SELECTED_STEP_EXTRA, step.getId());

        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.

            StepDetailActivityFragment fragment = new StepDetailActivityFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.detail_container, fragment, STEP_DETAIL_FRAGMENT_TAG)
                    .commit();

        } else {
            Intent intent = new Intent(RecipeDetailActivity.this, StepDetailActivity.class);
            intent.putExtras(args);
            startActivity(intent);
        }
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {

        if (id == RECIPE_LOADER) {
            // Individual recipe Uri
            Uri recipeUri = DatabaseContract.RecipeEntry.buildRecipeUri(selectedRecipeId);

            // Return cursor loader
            return new CursorLoader(this,
                    recipeUri,
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
        if (loaderId == RECIPE_LOADER) {

            // Move cursor to first result
            if (data.moveToFirst()) {
                // Set recipe name as toolbar title
                String recipeName = data.getString(COL_RECIPE_NAME);
                getSupportActionBar().setTitle(recipeName);
                // Set recipe image
                String recipeImageUrl = data.getString(COL_IMAGE_URL);
                binding.setRecipeImageUrl(recipeImageUrl);
                //String recipeName = data.getString(COL_RECIPE_NAME);
                //getSupportActionBar().setTitle(recipeName);
            }
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }
}
