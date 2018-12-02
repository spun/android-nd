package com.spundev.bakingtime.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by spundev
 */

public class Recipe implements Parcelable {

    @SerializedName("id")
    private final int id;
    @SerializedName("name")
    private final String name;
    @SerializedName("servings")
    private final int servings;
    @SerializedName("image")
    private final String image;
    @SerializedName("ingredients")
    private List<Ingredient> ingredients;
    @SerializedName("steps")
    private  List<Step> steps;
    private final int numSteps;
    private final int numIngredients;

    public Recipe(int id, String name, int servings, String image, int numSteps, int numIngredients) {
        this.id = id;
        this.name = name;
        this.servings = servings;
        this.image = image;
        this.ingredients = new ArrayList<>();
        this.steps = new ArrayList<>();
        this.numSteps = numSteps;
        this.numIngredients = numIngredients;
    }

    private Recipe(Parcel in) {
        id = in.readInt();
        name = in.readString();
        servings = in.readInt();
        image = in.readString();
        ingredients = new ArrayList<>();
        in.readList(ingredients, Ingredient.class.getClassLoader());
        steps = new ArrayList<>();
        in.readList(steps, Step.class.getClassLoader());
        numSteps = in.readInt();
        numIngredients = in.readInt();
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getServings() {
        return servings;
    }

    public String getImage() {
        return image;
    }

    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    public List<Step> getSteps() {
        return steps;
    }

    public int getNumSteps() {
        return numSteps;
    }

    public int getNumIngredients() {
        return numIngredients;
    }

    public void setIngredients(List<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }

    public void setSteps(List<Step> steps) {
        this.steps = steps;
    }

    @SuppressWarnings("unused")
    public static final Creator<Recipe> CREATOR = new Creator<Recipe>() {
        @Override
        public Recipe createFromParcel(Parcel in) {
            return new Recipe(in);
        }

        @Override
        public Recipe[] newArray(int size) {
            return new Recipe[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(name);
        parcel.writeInt(servings);
        parcel.writeString(image);
        parcel.writeList(ingredients);
        parcel.writeList(steps);
        parcel.writeInt(numSteps);
        parcel.writeInt(numIngredients);
    }


}
