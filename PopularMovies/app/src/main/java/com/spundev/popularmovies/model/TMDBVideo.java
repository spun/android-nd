package com.spundev.popularmovies.model;

import com.spundev.popularmovies.util.Youtube;

/**
 * Created by spundev.
 */

public class TMDBVideo {

    private final String name;
    private final String key;

    public TMDBVideo(String name, String key) {
        this.name = name;
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public String getKey() {
        return key;
    }

    public String getThumbnailUrl() {
        return Youtube.getVideoThumbnailUrlFromKey(this.key);
    }
}
