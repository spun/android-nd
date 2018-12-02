package com.spundev.capstone.db;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.spundev.capstone.db.dao.CardDao;
import com.spundev.capstone.model.Card;

public class SearchProvider extends ContentProvider {

    // From google samples github

    /**
     * The authority of this content provider.
     */
    private static final String AUTHORITY = "com.spundev.capstone.search";

    /**
     * The URI for the Cheese table.
     */
    public static final Uri URI_CHEESE = Uri.parse("content://" + AUTHORITY + "/" + Card.TABLE_NAME);

    /**
     * The match code for some items in the Cheese table.
     */
    private static final int CODE_CARD_DIR = 1;

    /**
     * The match code for an item in the Cheese table.
     */
    private static final int CODE_CARD_ITEM = 2;

    /**
     * Suggestions
     */
    private static final int CODE_CARD_SUGGEST = 3;

    /**
     * The URI matcher.
     */
    private static final UriMatcher MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        MATCHER.addURI(AUTHORITY, Card.TABLE_NAME, CODE_CARD_DIR);
        MATCHER.addURI(AUTHORITY, Card.TABLE_NAME + "/*", CODE_CARD_ITEM);
        MATCHER.addURI(AUTHORITY, "search_suggest_query", CODE_CARD_SUGGEST);
        MATCHER.addURI(AUTHORITY, "search_suggest_query" + "/*", CODE_CARD_SUGGEST);
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] strings, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        final int code = MATCHER.match(uri);
        switch (code) {
            case CODE_CARD_DIR:
            case CODE_CARD_ITEM:
                throw new IllegalArgumentException("Not used: " + uri);
            case CODE_CARD_SUGGEST:
                // retrieve the query text
                String query = uri.getLastPathSegment().toLowerCase();
                // Search
                final Context context = getContext();
                CardDao cardDao = AppRoomDatabase.getDatabase(context).cardDao();
                return cardDao.getSuggestions(query);
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (MATCHER.match(uri)) {
            case CODE_CARD_DIR:
                return "vnd.android.cursor.dir/" + AUTHORITY + "." + Card.TABLE_NAME;
            case CODE_CARD_ITEM:
                return "vnd.android.cursor.item/" + AUTHORITY + "." + Card.TABLE_NAME;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }
}
