package com.spundev.capstone.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.provider.BaseColumns;

@Entity(tableName = Card.TABLE_NAME,
        foreignKeys = @ForeignKey(
                entity = Category.class,
                parentColumns = "id",
                childColumns = "categoryId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = @Index("categoryId"))
public class Card {

    /** The name of the Cheese table. */
    public static final String TABLE_NAME = "card_table";

    /** The name of the ID column. */
    private static final String COLUMN_ID = BaseColumns._ID;

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(index = true, name = COLUMN_ID)
    private long id;
    private String text;
    private int categoryId;
    private boolean favourite;

    public Card(String text) {
        this.text = text;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public boolean isFavourite() {
        return favourite;
    }

    public void setFavourite(boolean favourite) {
        this.favourite = favourite;
    }
}
