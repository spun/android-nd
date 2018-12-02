package com.spundev.capstone.db.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.spundev.capstone.model.Category;

import java.util.List;

@Dao
public interface CategoryDao {

    @Insert
    void insert(Category word);

    @Query("SELECT * from category_table")
    LiveData<List<Category>> getAllCategories();

    @Update
    void updateCategory(Category category);

    @Delete
    void deleteCategory(Category category);

    @Query("DELETE FROM category_table")
    void deleteAll();
}
