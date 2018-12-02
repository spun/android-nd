package com.spundev.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.spundev.popularmovies.data.DatabaseContract.MovieEntry;

/**
 * Created by spundev.
 */

class DatabaseHelper extends SQLiteOpenHelper {

    // If we change the database schema, we must increment the database version.
    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "popular_movies.db";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        final String SQL_CREATE_MOVIE_TABLE = "CREATE TABLE " + MovieEntry.TABLE_NAME + " (" +
                MovieEntry._ID + " INTEGER PRIMARY KEY," +
                MovieEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_RELEASE_DATE + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_POSTER_URL + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_VOTE_AVERAGE + " REAL NOT NULL, " +
                MovieEntry.COLUMN_OVERVIEW + " TEXT UNIQUE NOT NULL " +
                " );";

        sqLiteDatabase.execSQL(SQL_CREATE_MOVIE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        // We discard the data and start over
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MovieEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}