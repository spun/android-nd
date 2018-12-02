package com.spundev.bakingtime.sync;

import android.content.Context;
import android.os.AsyncTask;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.spundev.bakingtime.apidata.ApiUtils;
import com.spundev.bakingtime.apidata.RecipesService;
import com.spundev.bakingtime.model.Recipe;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;

import retrofit2.Call;

/**
 * Created by spundev.
 */
public class RecipesSyncJobService extends JobService {

    private AsyncTask<Void, Void, Boolean> retrieveRecipesTask;

    @Override
    public boolean onStartJob(final JobParameters job) {
        // Do some work here
        // onStartJob runs on the main thread!

        // @see <a href="https://medium.com/google-developers/scheduling-jobs-like-a-pro-with-jobscheduler-286ef8510129"">Scheduling jobs like a pro with JobScheduler</a>
        retrieveRecipesTask = new RetrieveRecipesTask(this) {
            @Override
            protected void onPostExecute(Boolean success) {
                jobFinished(job, !success);
            }
        };
        retrieveRecipesTask.execute();
        return true; // Answers the question: "Is there still work going on?"
    }

    /**
     * Called when the scheduling engine has decided to interrupt the execution of a running job,
     * most likely because the runtime constraints associated with the job are no longer satisfied.
     */
    @Override
    public boolean onStopJob(JobParameters job) {
        // Cancel retrofit retrofitCall
        if (retrieveRecipesTask != null) {
            retrieveRecipesTask.cancel(true);
        }
        return true; // Answers the question: "Should this job be retried?"
    }


    private static class RetrieveRecipesTask extends AsyncTask<Void, Void, Boolean> {

        private final WeakReference<Context> contextRef;

        RetrieveRecipesTask(Context context) {
            contextRef = new WeakReference<>(context);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            Context context = contextRef.get();
            if (context != null) {
                RecipesService recipesService = ApiUtils.getRecipesService();
                Call<List<Recipe>> call = recipesService.getRecipes();

                try {
                    // retrieve list
                    List<Recipe> recipes = call.execute().body();
                    // insert into database
                    RecipesSyncUtils.insertIntoDatabase(context, recipes);
                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return false;
        }
    }
}
