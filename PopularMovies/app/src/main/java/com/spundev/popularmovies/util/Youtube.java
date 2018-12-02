package com.spundev.popularmovies.util;

/**
 * Created by spundev.
 */

public class Youtube {

    public static String getVideoUrlFromKey(String videoKey) {
        return "https://www.youtube.com/watch?v=" + videoKey;
    }

    public static String getShareableVideoUrlFromKey(String videoKey) {
        return "https://youtu.be/" + videoKey;
    }

    public static String getVideoThumbnailUrlFromKey(String videoKey) {
        return "https://img.youtube.com/vi/" + videoKey + "/hqdefault.jpg";
    }
}
