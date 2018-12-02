package com.spundev.bakingtime.sync;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.spundev.bakingtime.apidata.ApiUtils;
import com.spundev.bakingtime.apidata.RecipesService;
import com.spundev.bakingtime.model.Recipe;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;

/**
 * Created by spundev.
 */

// Intent service that downloads and parses the recipes json from the url
public class RecipesSyncIntentService extends IntentService {

    public RecipesSyncIntentService() {
        super("RecipesSyncIntentService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        // This is running on a background thread, we can do the retrofitCall synchronously
        RecipesService recipesService = ApiUtils.getRecipesService();
        Call<List<Recipe>> call = recipesService.getRecipes();

        try {
            // retrieve list
            List<Recipe> recipes = call.execute().body();
            // insert into database
            RecipesSyncUtils.insertIntoDatabase(this, recipes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
