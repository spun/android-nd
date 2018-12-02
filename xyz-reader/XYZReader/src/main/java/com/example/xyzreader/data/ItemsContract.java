package com.example.xyzreader.data;

import android.net.Uri;

public class ItemsContract {
    public static final String CONTENT_AUTHORITY = "com.example.xyzreader";
    private static final Uri BASE_URI = Uri.parse("content://com.example.xyzreader");

    interface ItemsColumns {
        /**
         * Type: INTEGER PRIMARY KEY AUTOINCREMENT
         */
        String _ID = "_id";
        /**
         * Type: TEXT
         */
        String SERVER_ID = "server_id";
        /**
         * Type: TEXT
         */
        String CONTENT_HASH = "content_hash";
        /**
         * Type: TEXT NOT NULL
         */
        String TITLE = "title";
        /**
         * Type: TEXT NOT NULL
         */
        String AUTHOR = "author";
        /**
         * Type: TEXT NOT NULL
         */
        String THUMB_URL = "thumb_url";
        /**
         * Type: TEXT NOT NULL
         */
        String PHOTO_URL = "photo_url";
        /**
         * Type: REAL NOT NULL DEFAULT 1.5
         */
        String ASPECT_RATIO = "aspect_ratio";
        /**
         * Type: INTEGER NOT NULL DEFAULT 0
         */
        String PUBLISHED_DATE = "published_date";
    }

    public static class Items implements ItemsColumns {
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.example.xyzreader.items";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.com.example.xyzreader.items";

        public static final String DEFAULT_SORT = PUBLISHED_DATE + " DESC";

        /**
         * Matches: /items/
         */
        public static Uri buildDirUri() {
            return BASE_URI.buildUpon().appendPath("items").build();
        }

        /**
         * Matches: /items/[_id]/
         */
        public static Uri buildItemUri(long _id) {
            return BASE_URI.buildUpon().appendPath("items").appendPath(Long.toString(_id)).build();
        }

        /**
         * Read item ID item detail URI.
         */
        public static long getItemId(Uri itemUri) {
            return Long.parseLong(itemUri.getPathSegments().get(1));
        }
    }

    interface ParagraphsColumns {
        String _ID = "_id";
        String TEXT = "paragraph";
        String POSITION = "position";
        String ITEM_ID = "item_id";
    }

    public static class Paragraphs implements ParagraphsColumns {
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.example.xyzreader.paragraphs";

        public static final String DEFAULT_SORT = POSITION + " ASC";

        /**
         * Matches: /items/[_id]/paragraphs/
         */
        public static Uri buildDirUri(long itemId) {
            return BASE_URI.buildUpon().appendPath("items").appendPath(Long.toString(itemId)).appendPath("paragraphs").build();
        }

        /**
         * Matches: /items/[_id]/paragraphs/[_id]/
         */
        public static Uri buildItemUri(long itemId, long paragraphId) {
            return BASE_URI.buildUpon().appendPath("items").appendPath(Long.toString(itemId)).appendPath("paragraphs").appendPath(Long.toString(paragraphId)).build();
        }

        public static int getItemIdFromUri(Uri uri) {
            // /recipe/*/step
            return Integer.parseInt(uri.getPathSegments().get(1));
        }
    }
}
