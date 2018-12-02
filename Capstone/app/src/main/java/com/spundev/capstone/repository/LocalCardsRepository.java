package com.spundev.capstone.repository;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;

import com.crashlytics.android.Crashlytics;
import com.spundev.capstone.db.AppRoomDatabase;
import com.spundev.capstone.db.dao.CardDao;
import com.spundev.capstone.model.Card;

import java.util.List;

import io.fabric.sdk.android.Fabric;

public class LocalCardsRepository {
    private final CardDao cardDao;
    private final LiveData<List<Card>> allCards;

    public LocalCardsRepository(Application application) {
        AppRoomDatabase db = AppRoomDatabase.getDatabase(application);
        cardDao = db.cardDao();
        allCards = cardDao.getAllCards();
    }

    public LiveData<List<Card>> getAllCards() {
        return allCards;
    }

    public LiveData<List<Card>> getAllCardsFromCategory(int categoryId) {
        return cardDao.getAllCardsFromCategory(categoryId);
    }

    public LiveData<List<Card>> getAllFavoriteCards() {
        return cardDao.getAllFavoriteCards();
    }

    public LiveData<List<Card>> getAllCardsFromQuery(String searchQuery) {
        return cardDao.getAllCardsFromQuery(searchQuery);
    }

    // CRUD public methods
    public void insert(Card card) {
        new insertAsyncTask(cardDao).execute(card);
    }

    public void delete(Card card) {
        new deleteAsyncTask(cardDao).execute(card);
    }

    public void update(Card card) {
        new updateAsyncTask(cardDao).execute(card);
    }




    // Insert new card
    private static class insertAsyncTask extends AsyncTask<Card, Void, Void> {

        private final CardDao asyncTaskDao;

        insertAsyncTask(CardDao dao) {
            asyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Card... params) {
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

    // Delete card from the database
    private static class deleteAsyncTask extends AsyncTask<Card, Void, Void> {

        private final CardDao asyncTaskDao;

        deleteAsyncTask(CardDao dao) {
            asyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Card... params) {
            try {
                asyncTaskDao.deleteCard(params[0]);
            } catch (SQLiteException ex) {
                if (Fabric.isInitialized()) {
                    Crashlytics.logException(ex);
                }
            }
            return null;
        }
    }

    // Update card from the database
    private static class updateAsyncTask extends AsyncTask<Card, Void, Void> {

        private final CardDao asyncTaskDao;

        updateAsyncTask(CardDao dao) {
            asyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Card... params) {
            try {
                asyncTaskDao.updateCard(params[0]);
            } catch (SQLiteException ex) {
                if (Fabric.isInitialized()) {
                    Crashlytics.logException(ex);
                }
            }
            return null;
        }
    }
}
