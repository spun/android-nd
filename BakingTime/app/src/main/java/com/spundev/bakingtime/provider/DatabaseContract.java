package com.spundev.bakingtime.provider;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by spundev.
 */

public class DatabaseContract {

    public static final String CONTENT_AUTHORITY = "com.spundev.bakingtime.provider";

    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_RECIPE = "recipe";
    public static final String PATH_INGREDIENT = "ingredient";
    public static final String PATH_STEP = "step";

    // Inner class that defines the table contents of the recipes table
    public static final class RecipeEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_RECIPE).build();

        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_RECIPE;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_RECIPE;

        // Table recipes
        public static final String TABLE_NAME = "recipes";
        public static final String COLUMN_NAME = "recipe_name";
        public static final String COLUMN_NUM_SERVINGS = "recipe_num_servings";
        public static final String COLUMN_IMAGE_URL = "recipe_image_url";
        public static final String COLUMN_NUM_STEPS = "recipe_num_steps";
        public static final String COLUMN_NUM_INGREDIENTS = "recipe_num_ingredients";

        public static Uri buildRecipeUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static String getRecipeIdFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }

    // Inner class that defines the table contents of the ingredients table
    public static final class IngredientEntry implements BaseColumns {

        // MIME types
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INGREDIENT;
        // public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INGREDIENT;

        // Table ingredients
        public static final String TABLE_NAME = "ingredients";
        public static final String COLUMN_RECIPE_ID = "ingredient_recipe_id";
        public static final String COLUMN_NAME = "ingredient_name";
        public static final String COLUMN_QUANTITY = "ingredient_quantity";
        public static final String COLUMN_MEASURE = "ingredient_measure";
        public static final String COLUMN_AVAILABLE = "ingredient_available";

        public static Uri buildIngredientUri(int recipeId, long ingredientId) {
            return RecipeEntry.CONTENT_URI.buildUpon().appendPath(Integer.toString(recipeId)).appendPath(PATH_INGREDIENT).appendPath(Long.toString(ingredientId)).build();
        }

        public static Uri buildIngredientsFromRecipeUri(int recipeId) {
            return RecipeEntry.CONTENT_URI.buildUpon().appendPath(Integer.toString(recipeId)).appendPath(PATH_INGREDIENT).build();
        }

        public static Integer getRecipeIdFromUri(Uri uri) {
            // /recipe/*/ingredient
            return Integer.parseInt(uri.getPathSegments().get(1));
        }
    }

    // Inner class that defines the table contents of the ingredients table
    public static final class StepEntry implements BaseColumns {

        // MIME types
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_STEP;
        // public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_STEP;

        // Table steps
        public static final String TABLE_NAME = "steps";
        public static final String COLUMN_RECIPE_ID = "step_recipe_id";
        public static final String COLUMN_SHORT_DESCRIPTION = "step_short_description";
        public static final String COLUMN_DESCRIPTION = "step_description";
        public static final String COLUMN_VIDEO_URL = "step_video_url";
        public static final String COLUMN_THUMBNAIL_URL = "step_thumbnail_url";

        public static Uri buildStepUri(int recipeId, long stepId) {
            return RecipeEntry.CONTENT_URI.buildUpon().appendPath(Integer.toString(recipeId)).appendPath(PATH_STEP).appendPath(Long.toString(stepId)).build();
        }

        public static Uri buildStepsFromRecipeUri(int recipeId) {
            return RecipeEntry.CONTENT_URI.buildUpon().appendPath(Integer.toString(recipeId)).appendPath(PATH_STEP).build();
        }

        public static int getRecipeIdFromUri(Uri uri) {
            // /recipe/*/step
            return Integer.parseInt(uri.getPathSegments().get(1));
        }
    }
}
