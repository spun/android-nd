package com.spundev.bakingtime.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by spundev.
 */

public class RecipesProvider extends ContentProvider {

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private DatabaseHelper mOpenHelper;

    private static final int RECIPE = 100;
    private static final int RECIPE_WITH_ID = 110;
    private static final int INGREDIENTS_FROM_RECIPE = 200;
    private static final int STEPS_FROM_RECIPE = 300;

    private static final SQLiteQueryBuilder sRecipeByIdQueryBuilder;
    private static final SQLiteQueryBuilder sIngredientsFromRecipeQueryBuilder;
    private static final SQLiteQueryBuilder sStepsFromRecipeQueryBuilder;

    static {
        // Retrieve recipe by id query builder
        sRecipeByIdQueryBuilder = new SQLiteQueryBuilder();
        sRecipeByIdQueryBuilder.setTables(DatabaseContract.RecipeEntry.TABLE_NAME);

        // Retrieve all ingredients from a recipe id query builder
        sIngredientsFromRecipeQueryBuilder = new SQLiteQueryBuilder();
        sIngredientsFromRecipeQueryBuilder.setTables(DatabaseContract.IngredientEntry.TABLE_NAME);

        // Retrieve all steps from a recipe id query builder
        sStepsFromRecipeQueryBuilder = new SQLiteQueryBuilder();
        sStepsFromRecipeQueryBuilder.setTables(DatabaseContract.StepEntry.TABLE_NAME);
    }


    // recipes.recipe_id = ?
    private static final String sRecipeIdSelection =
            DatabaseContract.RecipeEntry.TABLE_NAME +
                    "." + DatabaseContract.RecipeEntry._ID + " = ? ";

    // ingredients.ingredient_recipe_id = ?
    private static final String sIngredientsFromRecipeIdSelection =
            DatabaseContract.IngredientEntry.TABLE_NAME +
                    "." + DatabaseContract.IngredientEntry.COLUMN_RECIPE_ID + " = ? ";

    // steps.step_recipe_id = ?
    private static final String sStepsFromRecipeIdSelection =
            DatabaseContract.StepEntry.TABLE_NAME +
                    "." + DatabaseContract.StepEntry.COLUMN_RECIPE_ID + " = ? ";


    // Retrieve recipe by id
    private Cursor getRecipeById(Uri uri, String[] projection, String sortOrder) {
        String recipeId = DatabaseContract.RecipeEntry.getRecipeIdFromUri(uri);

        String[] selectionArgs;
        String selection;

        selectionArgs = new String[]{recipeId};
        selection = sRecipeIdSelection;

        return sRecipeByIdQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    // Retrieve all ingredients from a recipe id
    private Cursor getIngredientsFromRecipe(Uri uri, String[] projection, String sortOrder) {
        int recipeId = DatabaseContract.IngredientEntry.getRecipeIdFromUri(uri);

        String[] selectionArgs;
        String selection;

        selectionArgs = new String[]{Integer.toString(recipeId)};
        selection = sIngredientsFromRecipeIdSelection;

        return sIngredientsFromRecipeQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    // Retrieve all steps from a recipe id
    private Cursor getStepsFromRecipe(Uri uri, String[] projection, String sortOrder) {
        int recipeId = DatabaseContract.StepEntry.getRecipeIdFromUri(uri);

        String[] selectionArgs;
        String selection;

        selectionArgs = new String[]{Integer.toString(recipeId)};
        selection = sStepsFromRecipeIdSelection;

        return sStepsFromRecipeQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }


    private static UriMatcher buildUriMatcher() {
        // All paths added to the UriMatcher have a corresponding code to return when a match is
        // found.
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = DatabaseContract.CONTENT_AUTHORITY;

        // For each type of URI you want to add, create a corresponding code.
        // Recipes
        matcher.addURI(authority, DatabaseContract.PATH_RECIPE, RECIPE);
        matcher.addURI(authority, DatabaseContract.PATH_RECIPE + "/*", RECIPE_WITH_ID);
        // Ingredients
        matcher.addURI(authority, DatabaseContract.PATH_RECIPE + "/*/" + DatabaseContract.PATH_INGREDIENT, INGREDIENTS_FROM_RECIPE);
        // Steps
        matcher.addURI(authority, DatabaseContract.PATH_RECIPE + "/*/" + DatabaseContract.PATH_STEP, STEPS_FROM_RECIPE);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        // Switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            // "recipe"
            case RECIPE: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        DatabaseContract.RecipeEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            // "recipe/*"
            case RECIPE_WITH_ID: {
                retCursor = getRecipeById(uri, projection, sortOrder);
                break;
            }
            // "recipe/*/ingredient"
            case INGREDIENTS_FROM_RECIPE: {
                retCursor = getIngredientsFromRecipe(uri, projection, sortOrder);
                break;
            }
            // "recipe/*/step"
            case STEPS_FROM_RECIPE: {
                retCursor = getStepsFromRecipe(uri, projection, sortOrder);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (getContext() != null) {
            retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return retCursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case RECIPE:
                return DatabaseContract.RecipeEntry.CONTENT_TYPE;
            case RECIPE_WITH_ID:
                return DatabaseContract.RecipeEntry.CONTENT_ITEM_TYPE;
            case INGREDIENTS_FROM_RECIPE:
                return DatabaseContract.IngredientEntry.CONTENT_TYPE;
            case STEPS_FROM_RECIPE:
                return DatabaseContract.StepEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case RECIPE: {
                long _id = db.insert(DatabaseContract.RecipeEntry.TABLE_NAME, null, contentValues);
                if (_id > 0)
                    returnUri = DatabaseContract.RecipeEntry.buildRecipeUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case INGREDIENTS_FROM_RECIPE: {
                long _id = db.insert(DatabaseContract.IngredientEntry.TABLE_NAME, null, contentValues);
                if (_id > 0)
                    returnUri = DatabaseContract.IngredientEntry.buildIngredientUri(contentValues.getAsInteger(DatabaseContract.IngredientEntry.COLUMN_RECIPE_ID), _id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case STEPS_FROM_RECIPE: {
                long _id = db.insert(DatabaseContract.StepEntry.TABLE_NAME, null, contentValues);
                if (_id > 0)
                    returnUri = DatabaseContract.StepEntry.buildStepUri(contentValues.getAsInteger(DatabaseContract.StepEntry.COLUMN_RECIPE_ID), _id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (getContext() != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        // this makes delete all rows return the number of rows deleted
        if (null == selection) selection = "1";
        switch (match) {
            case RECIPE:
                rowsDeleted = db.delete(
                        DatabaseContract.RecipeEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case INGREDIENTS_FROM_RECIPE:
                rowsDeleted = db.delete(
                        DatabaseContract.IngredientEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case STEPS_FROM_RECIPE:
                rowsDeleted = db.delete(
                        DatabaseContract.StepEntry.TABLE_NAME, selection, selectionArgs);
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
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case INGREDIENTS_FROM_RECIPE:
                rowsUpdated = db.update(DatabaseContract.IngredientEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        switch (sUriMatcher.match(uri)) {

            case RECIPE: {
                db.beginTransaction();
                int rowsInserted = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(DatabaseContract.RecipeEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            rowsInserted++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                if (rowsInserted > 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }

                return rowsInserted;
            }
            case INGREDIENTS_FROM_RECIPE: {
                db.beginTransaction();
                int rowsInserted = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(DatabaseContract.IngredientEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            rowsInserted++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                if (rowsInserted > 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }

                return rowsInserted;
            }
            case STEPS_FROM_RECIPE: {
                db.beginTransaction();
                int rowsInserted = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(DatabaseContract.StepEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            rowsInserted++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                if (rowsInserted > 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }

                return rowsInserted;
            }
            default:
                return super.bulkInsert(uri, values);
        }
    }
}
