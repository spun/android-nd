package com.spundev.capstone.repository;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;

import com.crashlytics.android.Crashlytics;
import com.spundev.capstone.db.AppRoomDatabase;
import com.spundev.capstone.db.dao.CategoryDao;
import com.spundev.capstone.model.Category;

import java.util.List;

import io.fabric.sdk.android.Fabric;

public class LocalCategoriesRepository {

    private final CategoryDao categoryDao;
    private final LiveData<List<Category>> allCategories;

    public LocalCategoriesRepository(Application application) {
        AppRoomDatabase db = AppRoomDatabase.getDatabase(application);
        categoryDao = db.categoryDao();
        allCategories = categoryDao.getAllCategories();
    }

    public LiveData<List<Category>> getAllCategories() {
        return allCategories;
    }

    // CRUD public methods
    public void insert(Category category) {
        new insertAsyncTask(categoryDao).execute(category);
    }

    public void delete(Category category) {
        new deleteAsyncTask(categoryDao).execute(category);
    }

    public void update(Category category) {
        new updateAsyncTask(categoryDao).execute(category);
    }


    // Insert new category
    private static class insertAsyncTask extends AsyncTask<Category, Void, Void> {

        private final CategoryDao asyncTaskDao;

        insertAsyncTask(CategoryDao dao) {
            asyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Category... params) {
            try {
                asyncTaskDao.insert(params[0]);
            } catch (SQLiteException ex) {
                if (Fabric.isInitialized()) {
                    Crashlytics.logException(ex);
                }
            }
            return null;
        }
    }

    // Delete category from the database
    private static class deleteAsyncTask extends AsyncTask<Category, Void, Void> {

        private final CategoryDao asyncTaskDao;

        deleteAsyncTask(CategoryDao dao) {
            asyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Category... params) {
            try {
                asyncTaskDao.deleteCategory(params[0]);
            } catch (SQLiteException ex) {
                if (Fabric.isInitialized()) {
                    Crashlytics.logException(ex);
                }
            }
            return null;
        }
    }

    // Update category from the database
    private static class updateAsyncTask extends AsyncTask<Category, Void, Void> {

        private final CategoryDao asyncTaskDao;

        updateAsyncTask(CategoryDao dao) {
            asyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Category... params) {
            try {
                asyncTaskDao.updateCategory(params[0]);
            } catch (SQLiteException ex) {
                if (Fabric.isInitialized()) {
                    Crashlytics.logException(ex);
                }
            }
            return null;
        }
    }
}
