package com.spundev.bakingtime.widget;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.spundev.bakingtime.R;
import com.spundev.bakingtime.provider.DatabaseContract;

/**
 * Created by spundev.
 */

public class ListWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new ListRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}

class ListRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    private static final String TAG = "ListRemoteViewsFactory";

    // Columns projection
    private static final String[] INGREDIENT_COLUMNS = {
            DatabaseContract.IngredientEntry._ID,
            DatabaseContract.IngredientEntry.COLUMN_NAME,
            DatabaseContract.IngredientEntry.COLUMN_QUANTITY,
            DatabaseContract.IngredientEntry.COLUMN_MEASURE,
            DatabaseContract.IngredientEntry.COLUMN_AVAILABLE,
            DatabaseContract.IngredientEntry.COLUMN_RECIPE_ID
    };

    // These indices are tied to INGREDIENT_COLUMNS. If INGREDIENT_COLUMNS changes, these must change.
    private static final int COL_INGREDIENT_ID = 0;
    private static final int COL_INGREDIENT_NAME = 1;
    private static final int COL_INGREDIENT_QUANTITY = 2;
    private static final int COL_INGREDIENT_MEASURE = 3;
    private static final int COL_INGREDIENT_AVAILABLE = 4;
    private static final int COL_INGREDIENT_RECIPE_ID = 5;

    private final Context mContext;
    private Cursor mCursor;
    private final int appWidgetId;

    ListRemoteViewsFactory(Context applicationContext, Intent intent) {
        this.mContext = applicationContext;
        appWidgetId = Integer.valueOf(intent.getData().getSchemeSpecificPart());
    }

    @Override
    public void onCreate() {
    }


    @Override
    public void onDataSetChanged() {

        try {
            // Recipe id that the widget is showing
            int widgetRecipeId = ShoppingListWidgetConfigureActivity.loadWidgetPref(mContext, appWidgetId);
            // Uri for the ingredients
            Uri recipeIngredientsUri = DatabaseContract.IngredientEntry.buildIngredientsFromRecipeUri(widgetRecipeId);

            if (mCursor != null) mCursor.close();
            mCursor = mContext.getContentResolver().query(
                    recipeIngredientsUri,
                    INGREDIENT_COLUMNS,
                    null,
                    null,
                    null
            );
        } catch (Exception e) {
            Log.e(TAG, "onDataSetChanged: ", e);
        }
    }

    @Override
    public void onDestroy() {
        if (mCursor != null) {
            mCursor.close();
        }
    }

    @Override
    public int getCount() {
        if (mCursor == null) return 0;
        return mCursor.getCount();
    }

    @Override
    public RemoteViews getViewAt(int position) {

        if (mCursor == null || mCursor.getCount() == 0) return null;
        mCursor.moveToPosition(position);

        int ingredientId = mCursor.getInt(COL_INGREDIENT_ID);
        String ingredientName = mCursor.getString(COL_INGREDIENT_NAME);
        float ingredientQuantity = mCursor.getFloat(COL_INGREDIENT_QUANTITY);
        String ingredientMeasure = mCursor.getString(COL_INGREDIENT_MEASURE);
        boolean ingredientAvailable = mCursor.getInt(COL_INGREDIENT_AVAILABLE) != 0;
        int ingredientRecipeId = mCursor.getInt(COL_INGREDIENT_RECIPE_ID);

        RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.shopping_list_widget_item);
        views.setTextViewText(R.id.app_widget_ingredient_name, ingredientName);
        views.setTextViewText(R.id.app_widget_ingredient_quantity, ingredientQuantity + " " + ingredientMeasure);

        if (ingredientAvailable) {
            views.setImageViewResource(R.id.app_widget_ingredient_available, R.drawable.ic_check_box_black_24dp);
        } else {
            views.setImageViewResource(R.id.app_widget_ingredient_available, R.drawable.ic_check_box_outline_black_24dp);
        }

        // Fill in the onClick PendingIntent Template using the specific Id for each item individually
        Bundle extras = new Bundle();
        extras.putInt(UpdateWidgetReceiver.EXTRA_RECIPE_ID, ingredientRecipeId);
        extras.putInt(UpdateWidgetReceiver.EXTRA_INGREDIENT_ID, ingredientId);
        extras.putBoolean(UpdateWidgetReceiver.EXTRA_CHECK_STATE, !ingredientAvailable);
        Intent fillInIntent = new Intent();
        fillInIntent.putExtras(extras);
        views.setOnClickFillInIntent(R.id.app_widget_ingredient_available, fillInIntent);

        return views;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1; // Treat all items in the GridView the same
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }
}
