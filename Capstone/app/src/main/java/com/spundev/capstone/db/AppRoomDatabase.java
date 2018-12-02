package com.spundev.capstone.db;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.spundev.capstone.db.dao.CardDao;
import com.spundev.capstone.db.dao.CategoryDao;
import com.spundev.capstone.model.Card;
import com.spundev.capstone.model.Category;

@Database(entities = {Category.class, Card.class}, version = 5, exportSchema = false)
public abstract class AppRoomDatabase extends RoomDatabase {

    // App Dao
    public abstract CategoryDao categoryDao();

    public abstract CardDao cardDao();

    // Room database instance
    private static AppRoomDatabase INSTANCE;

    // get database singleton
    public static AppRoomDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppRoomDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppRoomDatabase.class, "app_database")
                            .addCallback(sRoomDatabaseCallback)
                            .fallbackToDestructiveMigration()   // todo: implement better migration
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static final RoomDatabase.Callback sRoomDatabaseCallback =
            new RoomDatabase.Callback() {
                @Override
                public void onCreate(@NonNull SupportSQLiteDatabase db) {
                    super.onCreate(db);
                    new PopulateDbAsync(INSTANCE).execute();
                }
            };

    // Initial database state
    private static class PopulateDbAsync extends AsyncTask<Void, Void, Void> {

        private final CategoryDao categoryDao;
        private final CardDao cardDao;

        PopulateDbAsync(AppRoomDatabase db) {
            categoryDao = db.categoryDao();
            cardDao = db.cardDao();
        }

        @Override
        protected Void doInBackground(final Void... params) {
            categoryDao.deleteAll();

            // Demo category
            Category category = new Category("Your first category", Color.parseColor("#2196f3"));
            category.setId(1);
            categoryDao.insert(category);

            // Demo cards
            Card cardWelcome = new Card("Welcome to tell-it. Here you can use and organize your cards");
            cardWelcome.setCategoryId(1);
            cardDao.insert(cardWelcome);

            Card cardName = new Card("My name is John");
            cardName.setCategoryId(1);
            cardDao.insert(cardName);

            Card cardNumber = new Card("My number is 555");
            cardNumber.setCategoryId(1);
            cardDao.insert(cardNumber);

            return null;
        }
    }
}
