package com.spundev.bakingtime.sync;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;
import android.util.Log;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.Trigger;
import com.spundev.bakingtime.model.Ingredient;
import com.spundev.bakingtime.model.Recipe;
import com.spundev.bakingtime.model.Step;
import com.spundev.bakingtime.provider.DatabaseContract;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

/**
 * Created by spundev.
 */
public class RecipesSyncUtils {

    private static final String TAG = "RecipesSyncUtils";

    // Interval at which to sync
    private static final int periodicity = (int) TimeUnit.DAYS.toSeconds(7); // Every week periodicity expressed as seconds
    private static final int toleranceInterval = (int) TimeUnit.HOURS.toSeconds(1); // a small(ish) window of time when triggering is OK

    private static boolean sInitialized;

    private static final String RECIPES_SYNC_TAG = "recipes-sync";

    /**
     * Schedules a repeating sync of recipes data using FirebaseJobDispatcher.
     */
    private static void scheduleFirebaseJobDispatcherSync(@NonNull final Context context) {

        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(context));

        // Create the Job to periodically sync the recipes
        Job syncSunshineJob = dispatcher.newJobBuilder()
                // The Service that will be used to retrieve the recipes
                .setService(RecipesSyncJobService.class)
                // Set the UNIQUE tag used to identify this Job
                .setTag(RECIPES_SYNC_TAG)
                // Network constraints on which this Job should run
                .setConstraints(Constraint.ON_ANY_NETWORK)
                // setLifetime sets how long this job should persist
                .setLifetime(Lifetime.FOREVER)
                // we want the data to stay up to date, so we tell this Job to recur
                .setRecurring(true)
                // We want the data to be synced every week
                .setTrigger(Trigger.executionWindow(periodicity, periodicity + toleranceInterval))
                // this new job will replace an old job if one with the same tag already exists
                .setReplaceCurrent(true)
                // Once the Job is ready, retrofitCall the builder's build method to return the Job
                .build();

        // Schedule the Job with the dispatcher
        dispatcher.schedule(syncSunshineJob);
    }

    /**
     * Creates periodic sync tasks and checks to see if an immediate sync is required.
     * This method is called from MainActivity onCreate()
     */
    synchronized public static void initialize(@NonNull final Context context) {
        // Only perform initialization once per app lifetime.
        if (sInitialized) return;
        sInitialized = true;

        // This method triggers the creation of a job dispatcher to synchronize data periodically
        scheduleFirebaseJobDispatcherSync(context);

        // We check if our ContentProvider has data
        CheckForEmptyTask checkForEmpty = new CheckForEmptyTask(context);
        checkForEmpty.execute();

        // TODO: check last sync limit
    }


    /**
     * Helper method to perform a sync immediately using an IntentService for asynchronous
     * execution.
     */
    public static void startImmediateSync(@NonNull final Context context) {
        Intent intentToSyncImmediately = new Intent(context, RecipesSyncIntentService.class);
        context.startService(intentToSyncImmediately);
    }

    /**
     * Helper method to perform an insertion of the list of recipes on the database.
     */
    @WorkerThread
    public static void insertIntoDatabase(Context context, List<Recipe> recipes) {

        ContentResolver recipesContentResolver = context.getContentResolver();

        for (Recipe r : recipes) {
            // Check if the recipe id already exists in the database
            // URI for recipes data
            Uri recipesQueryUri = DatabaseContract.RecipeEntry.buildRecipeUri(r.getId());
            // we just need the ID to see if we have any data
            String[] projectionColumns = {DatabaseContract.RecipeEntry._ID};
            // we perform the query to check to see if we already have the recipe data
            Cursor cursor = context.getContentResolver().query(
                    recipesQueryUri,
                    projectionColumns,
                    null,
                    null,
                    null);

            // If we don't have a recipe with the same id of the one we are trying to insert
            if (null == cursor || cursor.getCount() == 0) {
                // add to database
                try {
                    // add recipe
                    ContentValues newRecipeValues = new ContentValues();
                    newRecipeValues.put(DatabaseContract.RecipeEntry._ID, r.getId());
                    newRecipeValues.put(DatabaseContract.RecipeEntry.COLUMN_NAME, r.getName());
                    newRecipeValues.put(DatabaseContract.RecipeEntry.COLUMN_NUM_SERVINGS, r.getServings());
                    newRecipeValues.put(DatabaseContract.RecipeEntry.COLUMN_IMAGE_URL, r.getImage());
                    newRecipeValues.put(DatabaseContract.RecipeEntry.COLUMN_NUM_STEPS, r.getSteps().size());
                    newRecipeValues.put(DatabaseContract.RecipeEntry.COLUMN_NUM_INGREDIENTS, r.getIngredients().size());

                    // we can't do a bulk insert because steps and recipes have a foreign key
                    // restriction with recipes
                    // TODO: refactor to allow bulkInsert of recipes ¿remove foreign key?
                    recipesContentResolver.insert(DatabaseContract.RecipeEntry.CONTENT_URI, newRecipeValues);

                    // insert steps
                    List<Step> stepsList = r.getSteps();
                    if (stepsList.size() > 0) {
                        Vector<ContentValues> stepsContentValuesVector = new Vector<>(stepsList.size());
                        for (Step s : stepsList) {
                            ContentValues newStepValues = new ContentValues();
                            newStepValues.put(DatabaseContract.StepEntry.COLUMN_RECIPE_ID, r.getId());
                            newStepValues.put(DatabaseContract.StepEntry.COLUMN_SHORT_DESCRIPTION, s.getShortDescription());
                            newStepValues.put(DatabaseContract.StepEntry.COLUMN_DESCRIPTION, s.getDescription());
                            newStepValues.put(DatabaseContract.StepEntry.COLUMN_VIDEO_URL, s.getVideoURL());
                            newStepValues.put(DatabaseContract.StepEntry.COLUMN_THUMBNAIL_URL, s.getThumbnailURL());

                            stepsContentValuesVector.add(newStepValues);
                        }
                        if (stepsContentValuesVector.size() > 0) {
                            ContentValues[] contentValuesArray = new ContentValues[stepsContentValuesVector.size()];
                            stepsContentValuesVector.toArray(contentValuesArray);
                            recipesContentResolver.bulkInsert(DatabaseContract.StepEntry.buildStepsFromRecipeUri(r.getId()), contentValuesArray);
                        }
                    }

                    // insert ingredients
                    List<Ingredient> ingredientsList = r.getIngredients();
                    if (ingredientsList.size() > 0) {
                        Vector<ContentValues> ingredientsContentValuesVector = new Vector<>(ingredientsList.size());
                        for (Ingredient i : ingredientsList) {
                            ContentValues newIngredientValues = new ContentValues();
                            newIngredientValues.put(DatabaseContract.IngredientEntry.COLUMN_RECIPE_ID, r.getId());
                            newIngredientValues.put(DatabaseContract.IngredientEntry.COLUMN_NAME, i.getIngredient());
                            newIngredientValues.put(DatabaseContract.IngredientEntry.COLUMN_QUANTITY, i.getQuantity());
                            newIngredientValues.put(DatabaseContract.IngredientEntry.COLUMN_MEASURE, i.getMeasure());

                            ingredientsContentValuesVector.add(newIngredientValues);
                        }
                        if (ingredientsContentValuesVector.size() > 0) {
                            ContentValues[] contentValuesArray = new ContentValues[ingredientsContentValuesVector.size()];
                            ingredientsContentValuesVector.toArray(contentValuesArray);
                            recipesContentResolver.bulkInsert(DatabaseContract.IngredientEntry.buildIngredientsFromRecipeUri(r.getId()), contentValuesArray);
                        }
                    }
                } catch (SQLException sqlException) {
                    Log.e(TAG, "insertIntoDatabase: ", sqlException);
                }
            }

            // If the recipe already exists, we ignore it
            // Improvement: We could check differences an decide if we need to update the recipe

            // close the Cursor to avoid leaks
            if (cursor != null) {
                cursor.close();
            }
        }
    }


    private static class CheckForEmptyTask extends AsyncTask<Void, Void, Void> {

        private final WeakReference<Context> contextRef;

        CheckForEmptyTask(Context context) {
            contextRef = new WeakReference<>(context);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            // URI for recipes data
            Uri recipesQueryUri = DatabaseContract.RecipeEntry.CONTENT_URI;

            // we just need the ID to see if we have any data
            String[] projectionColumns = {DatabaseContract.RecipeEntry._ID};

            Context context = contextRef.get();
            if (context != null) {
                // we perform the query to check to see if we have any weather data
                Cursor cursor = context.getContentResolver().query(
                        recipesQueryUri,
                        projectionColumns,
                        null,
                        null,
                        null);

                if (null == cursor || cursor.getCount() == 0) {
                    startImmediateSync(context);
                }

                // close the Cursor to avoid leaks
                if (cursor != null) {
                    cursor.close();
                }
            }
            return null;
        }
    }
}
