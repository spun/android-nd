package com.spundev.bakingtime.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * Created by spundev
 */

public class Ingredient implements Parcelable {

    private final int id;
    @SerializedName("ingredient")
    private final String ingredient;
    @SerializedName("quantity")
    private final float quantity;
    @SerializedName("measure")
    private final String measure;
    private final boolean available;
    private final int recipeId;

    public Ingredient(int id, String name, float quantity, String measure, boolean available, int recipeId) {
        this.id = id;
        this.ingredient = name;
        this.quantity = quantity;
        this.measure = measure;
        this.available = available;
        this.recipeId = recipeId;
    }

    private Ingredient(Parcel in) {
        id = in.readInt();
        ingredient = in.readString();
        quantity = in.readFloat();
        measure = in.readString();
        available = in.readByte() != 0;
        recipeId = in.readInt();
    }

    public int getId() {
        return id;
    }

    public String getIngredient() {
        return ingredient;
    }

    public float getQuantity() {
        return quantity;
    }

    public String getMeasure() {
        return measure;
    }

    public boolean isAvailable() {
        return available;
    }

    public int getRecipeId() {
        return recipeId;
    }

    @SuppressWarnings("unused")
    public static final Creator<Ingredient> CREATOR = new Creator<Ingredient>() {
        @Override
        public Ingredient createFromParcel(Parcel in) {
            return new Ingredient(in);
        }

        @Override
        public Ingredient[] newArray(int size) {
            return new Ingredient[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(ingredient);
        parcel.writeFloat(quantity);
        parcel.writeString(measure);
        parcel.writeByte((byte) (available ? 1 : 0));
        parcel.writeInt(recipeId);
    }
}
