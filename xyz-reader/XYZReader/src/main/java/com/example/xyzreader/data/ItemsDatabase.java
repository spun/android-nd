package com.example.xyzreader.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.example.xyzreader.data.ItemsProvider.Tables;

public class ItemsDatabase extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "xyzreader.db";
    private static final int DATABASE_VERSION = 3;

    public ItemsDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            // Enable foreign key constraints
            db.execSQL("PRAGMA foreign_keys=ON;");
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + Tables.ITEMS + " ("
                + ItemsContract.ItemsColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + ItemsContract.ItemsColumns.SERVER_ID + " TEXT,"
                + ItemsContract.ItemsColumns.CONTENT_HASH + " TEXT,"
                + ItemsContract.ItemsColumns.TITLE + " TEXT NOT NULL,"
                + ItemsContract.ItemsColumns.AUTHOR + " TEXT NOT NULL,"
                + ItemsContract.ItemsColumns.THUMB_URL + " TEXT NOT NULL,"
                + ItemsContract.ItemsColumns.PHOTO_URL + " TEXT NOT NULL,"
                + ItemsContract.ItemsColumns.ASPECT_RATIO + " REAL NOT NULL DEFAULT 1.5,"
                + ItemsContract.ItemsColumns.PUBLISHED_DATE + " TEXT NOT NULL"
                + ")");

        db.execSQL("CREATE TABLE " + Tables.PARAGRAPHS + " ("
                + ItemsContract.ParagraphsColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + ItemsContract.ParagraphsColumns.TEXT + " TEXT,"
                + ItemsContract.ParagraphsColumns.POSITION + " INTEGER NOT NULL ,"
                + ItemsContract.ParagraphsColumns.ITEM_ID + " INTEGER NOT NULL "
                + "REFERENCES " + Tables.ITEMS + " (" + ItemsContract.ItemsColumns._ID + ") ON DELETE CASCADE "
                + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + Tables.ITEMS);
        db.execSQL("DROP TABLE IF EXISTS " + Tables.PARAGRAPHS);
        onCreate(db);
    }
}
