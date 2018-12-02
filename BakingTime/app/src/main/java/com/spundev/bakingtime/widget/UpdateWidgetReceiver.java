package com.spundev.bakingtime.widget;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;

import com.spundev.bakingtime.provider.DatabaseContract;

public class UpdateWidgetReceiver extends BroadcastReceiver {

    public static final String ACTION_CHECK_INGREDIENT = "com.spundev.bakingtime.action.check_ingredient";
    public static final String EXTRA_RECIPE_ID = "com.spundev.bakingtime.extra.recipe_id";
    public static final String EXTRA_INGREDIENT_ID = "com.spundev.bakingtime.extra.ingredient_id";
    public static final String EXTRA_CHECK_STATE = "com.spundev.bakingtime.extra.check_state";

    @Override
    public void onReceive(Context context, Intent intent) {
        // Called when a widget checkbox changes
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_CHECK_INGREDIENT.equals(action)) {
                int recipeId = intent.getIntExtra(EXTRA_RECIPE_ID, -1);
                int ingredientId = intent.getIntExtra(EXTRA_INGREDIENT_ID, -1);
                boolean checkState = intent.getBooleanExtra(EXTRA_CHECK_STATE, false);
                handleActionCheckIngredient(context, recipeId, ingredientId, checkState);
            }
        }
    }


    private void handleActionCheckIngredient(Context context, int recipeId, int ingredientId, boolean checkState) {

        // change availability
        ContentValues updatedValues = new ContentValues();
        updatedValues.put(DatabaseContract.IngredientEntry.COLUMN_AVAILABLE, checkState);
        // update database
        context.getContentResolver().update(
                DatabaseContract.IngredientEntry.buildIngredientsFromRecipeUri(recipeId),
                updatedValues,
                DatabaseContract.IngredientEntry._ID + "= ?",
                new String[]{Long.toString(ingredientId)}
        );
        //Now update all widgets
        ShoppingListWidgetProvider.updateShoppingListWidgets(context);
    }
}
