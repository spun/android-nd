package com.spundev.bakingtime;

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

import com.spundev.bakingtime.adapter.StepsAdapter;
import com.spundev.bakingtime.databinding.FragmentRecipeStepsBinding;
import com.spundev.bakingtime.model.Step;
import com.spundev.bakingtime.provider.DatabaseContract;


public class RecipeDetailStepsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    // Bundle argument for selected recipe id
    public static final String SELECTED_RECIPE = "SELECTED_RECIPE";
    private int selectedRecipe;

    // Loader id
    private static final int STEPS_LOADER = 30;

    // Columns projection
    private static final String[] STEP_COLUMNS = {
            DatabaseContract.StepEntry._ID,
            DatabaseContract.StepEntry.COLUMN_SHORT_DESCRIPTION,
            DatabaseContract.StepEntry.COLUMN_DESCRIPTION,
            DatabaseContract.StepEntry.COLUMN_VIDEO_URL,
            DatabaseContract.StepEntry.COLUMN_THUMBNAIL_URL
    };

    // These indices are tied to STEP_COLUMNS. If STEP_COLUMNS changes, these must change.
    public static final int COL_STEP_ID = 0;
    public static final int COL_STEP_SHORT_DESCRIPTION = 1;
    public static final int COL_STEP_DESCRIPTION = 2;
    public static final int COL_STEP_VIDEO_URL = 3;
    public static final int COL_STEP_THUMBNAIL_URL = 4;

    private RecyclerView stepsRecyclerView;
    private StepsAdapter stepsAdapter;
    private TextView emptyStepsTextView;

    public interface Callback {
        void onStepItemSelected(Step step);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Data binding
        FragmentRecipeStepsBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_recipe_steps, container, false);

        // retrieve the id of the selected recipe from the bundle
        Bundle arguments = getArguments();
        if (arguments != null) {
            selectedRecipe = arguments.getInt(SELECTED_RECIPE);
        }

        // RecyclerView
        stepsRecyclerView = binding.stepsRecyclerView;
        // Divider lines
        stepsRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));

        // Empty message
        emptyStepsTextView = binding.emptyStepsTextView;

       // LinearLayoutManager
        stepsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));

        // The adapter will take data from a source and
        // use it to populate the RecyclerView it's attached to.
        stepsAdapter = new StepsAdapter(new StepsAdapter.StepsAdapterOnClickHandler() {
            @Override
            public void onClick(Step step) {
                // Get selected step and start callback
                ((Callback) getActivity()).onStepItemSelected(step);
            }
        });
        stepsRecyclerView.setAdapter(stepsAdapter);

        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(STEPS_LOADER, null, this);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        if (id == STEPS_LOADER) {
            // Uri for all the steps on an recipe
            Uri savedMoviesUri = DatabaseContract.StepEntry.buildStepsFromRecipeUri(selectedRecipe);

            // Return cursor loader
            return new CursorLoader(getActivity(),
                    savedMoviesUri,
                    STEP_COLUMNS,
                    null,
                    null,
                    null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        int loaderId = loader.getId();
        if (loaderId == STEPS_LOADER) {
            // Send steps to the adapter
            stepsAdapter.swapCursor(data);

            // Check if we have data to show
            if (data.getCount() == 0) {
                stepsRecyclerView.setVisibility(View.GONE);
                emptyStepsTextView.setVisibility(View.VISIBLE);
            } else {
                stepsRecyclerView.setVisibility(View.VISIBLE);
                emptyStepsTextView.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        int loaderId = loader.getId();
        if (loaderId == STEPS_LOADER) {
            stepsAdapter.swapCursor(null);
        }
    }
}
