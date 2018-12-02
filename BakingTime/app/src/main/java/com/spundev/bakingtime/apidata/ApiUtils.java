package com.spundev.bakingtime.apidata;

/**
 * Created by spundev.
 */

public class ApiUtils {
    public static RecipesService getRecipesService() {
        return ServiceGenerator.createService(RecipesService.class);
    }
}
