package com.spundev.popularmovies.model;

/**
 * Created by spundev.
 */

public class TMDBReview {
    private final String author;
    private final String content;
    private boolean expanded;

    public TMDBReview(String author, String content) {
        this.author = author;
        this.content = content;
        this.expanded = false;
    }

    public String getAuthor() {
        return author;
    }

    public String getContent() {
        return content;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }
}
