package com.spundev.popularmovies.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by spundev.
 */

public class DatabaseContract {

    public static final String CONTENT_AUTHORITY = "com.spundev.popularmovies";

    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_MOVIE = "movie";

    /* Inner class that defines the table contents of the movies table */
    public static final class MovieEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIE).build();

        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE;

        // Table
        public static final String TABLE_NAME = "movie";
        public static final String COLUMN_TITLE = "movie_title";
        public static final String COLUMN_RELEASE_DATE = "movie_release_date";
        public static final String COLUMN_POSTER_URL = "movie_poster_url";
        public static final String COLUMN_VOTE_AVERAGE = "movie_vote_average";
        public static final String COLUMN_OVERVIEW = "movie_overview";


        public static Uri buildMovieUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static String getMovieIdFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }
}
