package com.spundev.bakingtime.apidata;

import com.spundev.bakingtime.model.Recipe;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Created by spundev
 */

public interface RecipesService {
    @GET("/android-baking-app-json")
    Call<List<Recipe>> getRecipes();
}
