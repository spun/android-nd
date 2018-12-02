package com.spundev.capstone.widget;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.spundev.capstone.R;
import com.spundev.capstone.db.AppRoomDatabase;
import com.spundev.capstone.db.dao.CardDao;

public class ListWidgetService extends RemoteViewsService {

    private static final String TAG = "ListRemoteViewsFactory";

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        Log.d(TAG, "onGetViewFactory: ");
        return new ListRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}

class ListRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    private static final String TAG = "ListRemoteViewsFactory";

    private final Context mContext;
    private Cursor mCursor;

    public ListRemoteViewsFactory(Context applicationContext, Intent intent) {
        this.mContext = applicationContext;
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDataSetChanged() {
        try {
            // Get data
            AppRoomDatabase db = AppRoomDatabase.getDatabase(mContext);
            CardDao cardDao = db.cardDao();
            mCursor = cardDao.getAllFavoriteCardsCursor();
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
        if (mCursor == null) return 1;
        return mCursor.getCount();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        if (mCursor == null || mCursor.getCount() == 0) return null;
        mCursor.moveToPosition(position);

        long favoriteId = mCursor.getInt(mCursor.getColumnIndex("_id"));
        String favoriteText = mCursor.getString(mCursor.getColumnIndex("text"));

        RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.favorites_list_widget_item);
        views.setTextViewText(R.id.app_widget_favorite_name, favoriteText);

        // Fill in the onClick PendingIntent Template using the specific Id for each item individually
        Bundle extras = new Bundle();
        extras.putLong(UpdateWidgetReceiver.EXTRA_FAVORITE_ID, favoriteId);
        extras.putString(UpdateWidgetReceiver.EXTRA_FAVORITE_TEXT, favoriteText);
        Intent fillInIntent = new Intent();
        fillInIntent.putExtras(extras);
        views.setOnClickFillInIntent(R.id.app_widget_favorite_name, fillInIntent);

        return views;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}