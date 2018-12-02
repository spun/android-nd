package com.spundev.capstone.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.TaskStackBuilder;
import android.widget.RemoteViews;

import com.spundev.capstone.R;
import com.spundev.capstone.ui.FavoritesActivity;

/**
 * Implementation of App Widget functionality.
 */
public class FavoritesWidget extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        CharSequence widgetText = context.getString(R.string.title_favorites_widget);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.favorites_widget);
        views.setTextViewText(R.id.widget_title_text, widgetText);

        // Title tap to show recipe details
        // Intent
        Intent recipeDetailIntent = new Intent(context, FavoritesActivity.class);
        // Stack
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntentWithParentStack(recipeDetailIntent);
        // Pending intent
        PendingIntent recipeDetailPendingIntent = stackBuilder.getPendingIntent(appWidgetId, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.widget_title_row, recipeDetailPendingIntent);

        // List items
        // Set the ListWidgetService intent to act as the adapter for the ListView
        Intent selectFavoriteIntent = new Intent(context, ListWidgetService.class);
        // Add the app widget ID to the intent. (https://stackoverflow.com/a/11387266)
        selectFavoriteIntent.setData(Uri.fromParts("content", String.valueOf(appWidgetId), null));
        views.setRemoteAdapter(R.id.favorites_list, selectFavoriteIntent);
        // Send to UpdateWidgetReceiver broadcast receiver when a favorite is clicked
        Intent favoriteClickedIntent = new Intent(context, UpdateWidgetReceiver.class);
        favoriteClickedIntent.setAction(UpdateWidgetReceiver.ACTION_CLICK_FAVORITE);
        // Pending intent
        PendingIntent checkBoxChangePendingIntent = PendingIntent.getBroadcast(context, 0, favoriteClickedIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setPendingIntentTemplate(R.id.favorites_list, checkBoxChangePendingIntent);

        // Handle empty messages
        views.setEmptyView(R.id.favorites_list, R.id.favorites_list_empty_textView);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
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

    public static void updateFavoritesWidgets(Context context) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, FavoritesWidget.class));
        //Trigger data update to handle the GridView widgets and force a data refresh
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.favorites_list);

        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }
}

