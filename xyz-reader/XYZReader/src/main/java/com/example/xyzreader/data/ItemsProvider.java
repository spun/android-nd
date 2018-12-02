
package com.example.xyzreader.data;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;

public class ItemsProvider extends ContentProvider {

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private SQLiteOpenHelper mOpenHelper;

    interface Tables {
        String ITEMS = "items";
        String PARAGRAPHS = "paragraphs";
    }

    private static final int ITEMS = 0;
    private static final int ITEMS__ID = 1;
    private static final int PARAGRAPHS_FROM_ITEM = 2;


    private static final SQLiteQueryBuilder sItemByIdQueryBuilder;
    private static final SQLiteQueryBuilder sParagraphsFromItemQueryBuilder;


    static {
        // Retrieve item by id query builder
        sItemByIdQueryBuilder = new SQLiteQueryBuilder();
        sItemByIdQueryBuilder.setTables(Tables.ITEMS);

        // Retrieve all paragraphs from a item id query builder
        sParagraphsFromItemQueryBuilder = new SQLiteQueryBuilder();
        sParagraphsFromItemQueryBuilder.setTables(Tables.PARAGRAPHS);
    }

    // items._id = ?
    private static final String sItemIdSelection =
            Tables.ITEMS +
                    "." + ItemsContract.Items._ID + " = ? ";

    // paragraphs.item_id = ?
    private static final String sParagraphsFromItemSelection =
            Tables.PARAGRAPHS +
                    "." + ItemsContract.Paragraphs.ITEM_ID + " = ? ";

    // Retrieve item by id
    private Cursor getItemById(Uri uri, String[] projection, String sortOrder) {
        long itemId = ItemsContract.Items.getItemId(uri);

        String[] selectionArgs;
        String selection;

        selectionArgs = new String[]{String.valueOf(itemId)};
        selection = sItemIdSelection;

        return sItemByIdQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    // Retrieve all ingredients from a recipe id
    private Cursor getParagraphsFromItem(Uri uri, String[] projection, String sortOrder) {
        int recipeId = ItemsContract.Paragraphs.getItemIdFromUri(uri);

        String[] selectionArgs;
        String selection;

        selectionArgs = new String[]{Integer.toString(recipeId)};
        selection = sParagraphsFromItemSelection;

        return sParagraphsFromItemQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }


    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = ItemsContract.CONTENT_AUTHORITY;
        // Items
        matcher.addURI(authority, "items", ITEMS);
        matcher.addURI(authority, "items/#", ITEMS__ID);
        // Paragraphs
        matcher.addURI(authority, "items/#/paragraphs", PARAGRAPHS_FROM_ITEM);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new ItemsDatabase(getContext());
        return true;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                return ItemsContract.Items.CONTENT_TYPE;
            case ITEMS__ID:
                return ItemsContract.Items.CONTENT_ITEM_TYPE;
            case PARAGRAPHS_FROM_ITEM:
                return ItemsContract.Paragraphs.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        // Switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor = null;
        switch (sUriMatcher.match(uri)) {
            case ITEMS: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        Tables.ITEMS,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case ITEMS__ID: {
                retCursor = getItemById(uri, projection, sortOrder);
                break;
            }
            case PARAGRAPHS_FROM_ITEM: {
                retCursor = getParagraphsFromItem(uri, projection, sortOrder);
                break;
            }
        }
        if (getContext() != null) {
            retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return retCursor;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues contentValues) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;
        switch (match) {
            case ITEMS: {
                long _id = db.insert(Tables.ITEMS, null, contentValues);
                if (_id > 0)
                    returnUri = ItemsContract.Items.buildItemUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case PARAGRAPHS_FROM_ITEM: {
                long _id = db.insert(Tables.PARAGRAPHS, null, contentValues);
                if (_id > 0)
                    returnUri = ItemsContract.Paragraphs.buildItemUri(ItemsContract.Items.getItemId(uri), _id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default: {
                throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        }
        if (getContext() != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        // this makes delete all rows return the number of rows deleted
        if (null == selection) selection = "1";
        switch (match) {
            case ITEMS:
                rowsDeleted = db.delete(Tables.ITEMS, selection, selectionArgs);
                break;
            case ITEMS__ID:
                rowsDeleted = db.delete(Tables.ITEMS, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }


    /**
     * Apply the given set of {@link ContentProviderOperation}, executing inside
     * a {@link SQLiteDatabase} transaction. All changes will be rolled back if
     * any single one fails.
     */
    @NonNull
    public ContentProviderResult[] applyBatch(@NonNull ArrayList<ContentProviderOperation> operations)
            throws OperationApplicationException {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            final int numOperations = operations.size();
            final ContentProviderResult[] results = new ContentProviderResult[numOperations];
            for (int i = 0; i < numOperations; i++) {
                results[i] = operations.get(i).apply(this, results, i);
            }
            db.setTransactionSuccessful();
            return results;
        } finally {
            db.endTransaction();
        }
    }
}
