package com.spundev.capstone.db.dao;

import android.app.SearchManager;
import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;
import android.database.Cursor;
import android.provider.BaseColumns;

import com.spundev.capstone.model.Card;

import java.util.List;

@Dao
public interface CardDao {

    @Insert
    void insert(Card word);

    @Query("SELECT * from card_table")
    LiveData<List<Card>> getAllCards();

    @Query("SELECT * from card_table where categoryId = :categoryId")
    LiveData<List<Card>> getAllCardsFromCategory(int categoryId);

    @Update
    void updateCard(Card card);

    @Delete
    void deleteCard(Card card);

    @Query("SELECT * from card_table WHERE favourite = 1")
    LiveData<List<Card>> getAllFavoriteCards();

    @Query("SELECT _id as " + BaseColumns._ID + " , text as " + SearchManager.SUGGEST_COLUMN_TEXT_2 + ", text as " + SearchManager.SUGGEST_COLUMN_INTENT_DATA + " from card_table where text LIKE '%' || :query || '%' limit 5")
    Cursor getSuggestions(String query);

    @Query("SELECT * from card_table where text LIKE '%' || :searchQuery || '%'")
    LiveData<List<Card>> getAllCardsFromQuery(String searchQuery);

    @Query("SELECT * from card_table WHERE favourite = 1")
    Cursor getAllFavoriteCardsCursor();
}
