package com.spundev.capstone.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.ColorInt;

@Entity(tableName = "category_table")
public class Category {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name;
    private int color;

    public Category(String name, @ColorInt int color) {
        this.name = name;
        this.color = color;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }
}
