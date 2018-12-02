package com.spundev.bakingtime.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.TaskStackBuilder;
import android.widget.RemoteViews;

import com.spundev.bakingtime.R;
import com.spundev.bakingtime.RecipeDetailActivity;
import com.spundev.bakingtime.provider.DatabaseContract;

import java.lang.ref.WeakReference;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link ShoppingListWidgetConfigureActivity ShoppingListWidgetConfigureActivity}
 */
public class ShoppingListWidgetProvider extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {

        // Recipe id that the widget is showing
        int widgetRecipeId = ShoppingListWidgetConfigureActivity.loadWidgetPref(context, appWidgetId);

        // View
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.shopping_list_widget);

        // Title tap to show recipe details
        // Intent
        Intent recipeDetailIntent = new Intent(context, RecipeDetailActivity.class);
        recipeDetailIntent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, widgetRecipeId);
        // Stack
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntentWithParentStack(recipeDetailIntent);
        // Pending intent
        PendingIntent recipeDetailPendingIntent = stackBuilder.getPendingIntent(appWidgetId, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.widget_title_row, recipeDetailPendingIntent);

        // Widget setting
        // Intent
        Intent settingsIntent = new Intent(context, ShoppingListWidgetConfigureActivity.class);
        settingsIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        settingsIntent.putExtra(ShoppingListWidgetConfigureActivity.EXTRA_APPWIDGET_UPDATE_MODE, true);
        // Pending intent
        PendingIntent pendingIntent = PendingIntent.getActivity(context, appWidgetId, settingsIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.widget_settings, pendingIntent);

        // List items
        // Set the ListWidgetService intent to act as the adapter for the ListView
        Intent selectIngredientIntent = new Intent(context, ListWidgetService.class);
        // Add the app widget ID to the intent. (https://stackoverflow.com/a/11387266)
        selectIngredientIntent.setData(Uri.fromParts("content", String.valueOf(appWidgetId), null));
        views.setRemoteAdapter(R.id.shopping_list, selectIngredientIntent);
        // Send to UpdateWidgetReceiver broadcast receiver when a checkbox is clicked
        Intent checkBoxChangeIntent = new Intent(context, UpdateWidgetReceiver.class);
        checkBoxChangeIntent.setAction(UpdateWidgetReceiver.ACTION_CHECK_INGREDIENT);
        // Pending intent
        PendingIntent checkBoxChangePendingIntent = PendingIntent.getBroadcast(context, 0, checkBoxChangeIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setPendingIntentTemplate(R.id.shopping_list, checkBoxChangePendingIntent);

        // Handle empty messages
        views.setEmptyView(R.id.shopping_list, R.id.shopping_list_empty_textView);

        // Async task to retrieve the recipe name from database an set it as title
        // Important!: The call to "updateAppWidget" is inside of the "onPostExecute" of the AsyncTask
        AsyncTask<Void, Void, String> retrieveRecipesTask = new RetrieveRecipeTitleTask(context, appWidgetManager, appWidgetId, views);
        retrieveRecipesTask.execute();
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    public static void updateShoppingListWidgets(Context context) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, ShoppingListWidgetProvider.class));
        //Trigger data update to handle the GridView widgets and force a data refresh
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.shopping_list);

        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }


    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the preference associated with it.
        for (int appWidgetId : appWidgetIds) {
            ShoppingListWidgetConfigureActivity.deleteWidgetPref(context, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }


    private static class RetrieveRecipeTitleTask extends AsyncTask<Void, Void, String> {

        // Context reference
        private final WeakReference<Context> contextRef;
        private final int appWidgetId;
        final AppWidgetManager appWidgetManager;
        final RemoteViews widgetView;

        RetrieveRecipeTitleTask(Context context, AppWidgetManager appWidgetManager, int appWidgetId, RemoteViews views) {
            contextRef = new WeakReference<>(context);
            this.appWidgetId = appWidgetId;
            this.appWidgetManager = appWidgetManager;
            widgetView = views;
        }

        @Override
        protected String doInBackground(Void... voids) {

            // Default widget title
            String recipeNameResult = "Shopping list";

            Context context = contextRef.get();
            if (context != null) {

                // Recipe id that the widget is showing
                int widgetRecipeId = ShoppingListWidgetConfigureActivity.loadWidgetPref(context, appWidgetId);

                // URI for recipe data
                Uri recipesQueryUri = DatabaseContract.RecipeEntry.buildRecipeUri(widgetRecipeId);

                // we just need the NAME of the recipe
                String[] projectionColumns = {DatabaseContract.RecipeEntry.COLUMN_NAME};

                // we perform the query to check to see if we have any weather data
                Cursor cursor = context.getContentResolver().query(
                        recipesQueryUri,
                        projectionColumns,
                        null,
                        null,
                        null);

                if (cursor != null && cursor.getCount() != 0) {
                    cursor.moveToNext();
                    int nameColumnIndex = cursor.getColumnIndex(DatabaseContract.RecipeEntry.COLUMN_NAME);
                    recipeNameResult = cursor.getString(nameColumnIndex);
                }

                // close the Cursor to avoid leaks
                if (cursor != null) {
                    cursor.close();
                }
            }
            return recipeNameResult;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            widgetView.setTextViewText(R.id.widget_title_text, s);
            appWidgetManager.updateAppWidget(appWidgetId, widgetView);
        }
    }
}

