package com.spundev.bakingtime.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by spundev
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    // If we change the database schema, we must increment the database version.
    private static final int DATABASE_VERSION = 7;
    public static final String DATABASE_NAME = "baking_time.db";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        final String SQL_CREATE_RECIPES_TABLE = "CREATE TABLE " + DatabaseContract.RecipeEntry.TABLE_NAME + " (" +
                DatabaseContract.RecipeEntry._ID + " INTEGER PRIMARY KEY," +
                DatabaseContract.RecipeEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                DatabaseContract.RecipeEntry.COLUMN_NUM_SERVINGS + " INTEGER NOT NULL, " +
                DatabaseContract.RecipeEntry.COLUMN_IMAGE_URL + " TEXT NOT NULL, " +
                DatabaseContract.RecipeEntry.COLUMN_NUM_STEPS + " INTEGER NOT NULL, " +
                DatabaseContract.RecipeEntry.COLUMN_NUM_INGREDIENTS + " INTEGER NOT NULL " +
                " );";

        final String SQL_CREATE_INGREDIENTS_TABLE = "CREATE TABLE " + DatabaseContract.IngredientEntry.TABLE_NAME + " (" +
                DatabaseContract.IngredientEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                DatabaseContract.IngredientEntry.COLUMN_RECIPE_ID + " INTEGER NOT NULL, " +
                DatabaseContract.IngredientEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                DatabaseContract.IngredientEntry.COLUMN_QUANTITY + " FLOAT NOT NULL, " +
                DatabaseContract.IngredientEntry.COLUMN_MEASURE + " TEXT NOT NULL, " +
                DatabaseContract.IngredientEntry.COLUMN_AVAILABLE + " BOOLEAN, " +
                "FOREIGN KEY(" + DatabaseContract.IngredientEntry.COLUMN_RECIPE_ID + ") REFERENCES " +
                DatabaseContract.RecipeEntry.TABLE_NAME + "(" + DatabaseContract.RecipeEntry._ID + ")" +
                " );";

        final String SQL_CREATE_STEPS_TABLE = "CREATE TABLE " + DatabaseContract.StepEntry.TABLE_NAME + " (" +
                DatabaseContract.StepEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                DatabaseContract.StepEntry.COLUMN_RECIPE_ID + " INTEGER NOT NULL, " +
                DatabaseContract.StepEntry.COLUMN_SHORT_DESCRIPTION + " TEXT NOT NULL, " +
                DatabaseContract.StepEntry.COLUMN_DESCRIPTION + " INTEGER NOT NULL, " +
                DatabaseContract.StepEntry.COLUMN_VIDEO_URL + " TEXT, " +
                DatabaseContract.StepEntry.COLUMN_THUMBNAIL_URL + " TEXT NOT NULL, " +
                "FOREIGN KEY(" + DatabaseContract.StepEntry.COLUMN_RECIPE_ID + ") REFERENCES " +
                DatabaseContract.RecipeEntry.TABLE_NAME + "(" + DatabaseContract.RecipeEntry._ID + ")" +
                " );";

        sqLiteDatabase.execSQL(SQL_CREATE_RECIPES_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_INGREDIENTS_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_STEPS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        // We discard the data and start over
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.RecipeEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.IngredientEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.StepEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
