package com.spundev.capstone.model.firestore;

@SuppressWarnings("SpellCheckingInspection")
public class CardFirestore {
    private String id;
    private String text;
    private int score;

    public CardFirestore() {
    }  // Needed for Firebase

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}
