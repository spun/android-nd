package com.example.xyzreader.data;

import android.app.IntentService;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.RemoteException;
import android.util.Log;

import com.example.xyzreader.remote.RemoteEndpointUtil;
import com.example.xyzreader.util.HashUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class UpdaterService extends IntentService {
    private static final String TAG = "UpdaterService";

    public static final String BROADCAST_ACTION_STATE_CHANGE
            = "com.example.xyzreader.intent.action.STATE_CHANGE";
    public static final String EXTRA_REFRESHING
            = "com.example.xyzreader.intent.extra.REFRESHING";

    public UpdaterService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        // Check connectivity before fetching
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni == null || !ni.isConnected()) {
            Log.w(TAG, "Not online, not refreshing.");
            // Send "end refresh" broadcast
            sendStickyBroadcast(new Intent(BROADCAST_ACTION_STATE_CHANGE).putExtra(EXTRA_REFRESHING, false));
            return;
        }

        // Send "start refresh" broadcast
        sendStickyBroadcast(new Intent(BROADCAST_ACTION_STATE_CHANGE).putExtra(EXTRA_REFRESHING, true));

        // Create an array of "ContentProviderOperations" to insert all the items in batch
        ArrayList<ContentProviderOperation> cpoItems = new ArrayList<>();
        // We create another array of "" for the paragraphs of each article to run it after the one
        // where we create the items and make sure that the foreign key between tables is fulfilled.
        ArrayList<ContentProviderOperation> cpoParagraphs = new ArrayList<>();

        // Uri Dir of articles
        Uri itemsDirUri = ItemsContract.Items.buildDirUri();

        // Delete all items [NOT NEEDED ANYMORE]
        // cpo.add(ContentProviderOperation.newDelete(dirUri).build());

        // We get all the content hashes (md5) from the articles in the database. We will check if
        // the content that we have in the database differs from the one that we are receiving by
        // comparing this hashes
        HashMap<String, Long> itemsHashList = getLocalItemsHashes();

        try {
            // Get remote content
            JSONArray array = RemoteEndpointUtil.fetchJsonArray();
            if (array == null) {
                throw new JSONException("Invalid parsed item array");
            }

            // For each article received
            for (int i = 0; i < array.length(); i++) {
                // Get article content
                JSONObject object = array.getJSONObject(i);
                // Calculate hash of the content
                String contentHash = HashUtils.md5(object.toString());

                // Check if we have already an article in the database with that same content
                if (itemsHashList.containsKey(contentHash)) {
                    // If we have if, remove from the hash list and skip insert. The remaining
                    // items in this HashMap will be removed from the database to make sure that if
                    // one item is removed from the remote list
                    itemsHashList.remove(contentHash);
                } else {
                    // If we didn't have this article in the database, create content values and add
                    // to the "ContentProviderOperations" for the items
                    ContentValues values = new ContentValues();
                    values.put(ItemsContract.Items._ID, object.getString("id"));
                    values.put(ItemsContract.Items.SERVER_ID, object.getString("id"));
                    values.put(ItemsContract.Items.CONTENT_HASH, contentHash);
                    values.put(ItemsContract.Items.AUTHOR, object.getString("author"));
                    values.put(ItemsContract.Items.TITLE, object.getString("title"));
                    values.put(ItemsContract.Items.THUMB_URL, object.getString("thumb"));
                    values.put(ItemsContract.Items.PHOTO_URL, object.getString("photo"));
                    values.put(ItemsContract.Items.ASPECT_RATIO, object.getString("aspect_ratio"));
                    values.put(ItemsContract.Items.PUBLISHED_DATE, object.getString("published_date"));
                    // Add to the "ContentProviderOperations"
                    cpoItems.add(ContentProviderOperation.newInsert(itemsDirUri).withValues(values).build());

                    // IMPORTANT: We are going to split the text of the article into paragraphs and
                    // save each paragraph as a row in a separate database. This will allow as to
                    // retrieve only a part of the text using a recycler view improving reducing the
                    // memory and the "time to paint" when an article is selected.

                    // Original text as receiver
                    String bodyTextRaw = object.getString("body");
                    // Remove all single line jumps
                    String bodyText = bodyTextRaw.replaceAll("(\\r\\n|\\n)(?!\\r\\n|\\n)", "");
                    // Split text into paragraphs
                    String[] bodyTextSplits = bodyText.split("(\\r\\n|\\n)");
                    // For each paragraph
                    for (int j = 0; j < bodyTextSplits.length; j++) {
                        // Create ContentValues for the new paragraph
                        ContentValues newParagraphValues = new ContentValues();
                        newParagraphValues.put(ItemsContract.Paragraphs.TEXT, bodyTextSplits[j]);
                        newParagraphValues.put(ItemsContract.Paragraphs.POSITION, j);
                        newParagraphValues.put(ItemsContract.Paragraphs.ITEM_ID, object.getString("id"));
                        // Add to the "ContentProviderOperations" for the paragraphs
                        cpoParagraphs.add(
                                ContentProviderOperation.newInsert(
                                        ItemsContract.Paragraphs.buildDirUri(Long.valueOf(object.getString("id")))
                                ).withValues(newParagraphValues).build());
                    }
                }
            }

            // At this point, the itemHashMap list will only have those articles from the local database
            // that don't exist in the remote data that we received. Those articles were probably deleted from
            // the remote database and we should do the same with the ones in our local database.
            deleteItemsFromHashMap(itemsHashList);

            // Run all ContentProviderOperations
            // Items/Articles
            if (!cpoItems.isEmpty()) {
                getContentResolver().applyBatch(ItemsContract.CONTENT_AUTHORITY, cpoItems);
            }
            // Paragraphs
            if (!cpoParagraphs.isEmpty()) {
                getContentResolver().applyBatch(ItemsContract.CONTENT_AUTHORITY, cpoParagraphs);
            }

        } catch (JSONException | RemoteException | OperationApplicationException e) {
            Log.e(TAG, "Error updating content.", e);
        }

        // Send "end refresh" broadcast
        sendStickyBroadcast(new Intent(BROADCAST_ACTION_STATE_CHANGE).putExtra(EXTRA_REFRESHING, false));
    }


    // Returns the Hashes (md5) of each article in our local database to simplify the action of checking
    // if we already have the same article saved
    private HashMap<String, Long> getLocalItemsHashes() {
        // Articles uri
        Uri itemsQueryUri = ItemsContract.Items.buildDirUri();

        // We just need the ID and the HASH of the content
        String[] projectionColumns = {ItemsContract.Items._ID, ItemsContract.Items.CONTENT_HASH};

        // Perform the query
        Cursor cursor = getContentResolver().query(
                itemsQueryUri,
                projectionColumns,
                null,
                null,
                null);

        // We create a new HashMap to save the hash of the content as key, and the id (to recreate
        // the uri of the item) as a value
        HashMap<String, Long> hashList = new HashMap<>();

        if (cursor != null) {
            // Add each result to the HashMap
            if (cursor.moveToFirst()) {
                do {
                    long itemId = cursor.getLong(0);
                    String itemHash = cursor.getString(1);
                    hashList.put(itemHash, itemId);
                }
                while (cursor.moveToNext());
            }
        }

        // close the Cursor to avoid leaks
        if (cursor != null) {
            cursor.close();
        }
        return hashList;
    }

    // Remove from database the articles that weren't found in the remote data using the same
    // HashMap (with hash and id) format from "getLocalItemsHashes"
    private void deleteItemsFromHashMap(HashMap<String, Long> hashList) {

        try {
            // New array of "ContentProviderOperations" to delete all the items in batch
            ArrayList<ContentProviderOperation> contentProviderOperations = new ArrayList<>();
            // For each article, add operation to the ContentProviderOperations array
            for (Map.Entry<String, Long> entry : hashList.entrySet()) {
                Long value = entry.getValue();
                // Add operation
                contentProviderOperations.add(
                        ContentProviderOperation.newDelete(ItemsContract.Items.buildItemUri(value)).build());
            }
            // Delete all from the HashMap in batch
            getContentResolver().applyBatch(ItemsContract.CONTENT_AUTHORITY, contentProviderOperations);
        } catch (RemoteException | OperationApplicationException e) {
            e.printStackTrace();
        }
    }
}
