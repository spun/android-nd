package com.spundev.capstone.model.firestore;

@SuppressWarnings("SpellCheckingInspection")
public class CategoryFirestore {
    private String id;
    private String name;
    private String description;
    private String color;

    public CategoryFirestore() {
    }  // Needed for Firebase

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
