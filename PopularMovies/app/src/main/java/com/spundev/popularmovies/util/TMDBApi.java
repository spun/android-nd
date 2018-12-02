package com.spundev.popularmovies.util;

import com.spundev.popularmovies.model.TMDBMovie;
import com.spundev.popularmovies.model.TMDBReview;
import com.spundev.popularmovies.model.TMDBVideo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by spundev.
 */

public class TMDBApi {

    private static final String MOVIE_POSTER_BASE_URL = "http://image.tmdb.org/t/p/w185/";

    public static List<TMDBMovie> getMoviesFromJson(String movieDataJsonStr) throws JSONException {
        // These are the names of the JSON objects that need to be extracted.
        final String MDB_LIST = "results";
        final String MDB_ID = "id";
        final String MDB_TITLE = "original_title";
        final String MDB_POSTER_PATH = "poster_path";
        final String MDB_VOTE_AVERAGE = "vote_average";
        final String MDB_RELEASE_DATE = "release_date";
        final String MDB_OVERVIEW = "overview";

        JSONObject moviesJson = new JSONObject(movieDataJsonStr);
        JSONArray movieResultsArray = moviesJson.getJSONArray(MDB_LIST);

        List<TMDBMovie> resultsList = new ArrayList<>();

        for (int i = 0; i < movieResultsArray.length(); i++) {

            JSONObject movieJson = movieResultsArray.getJSONObject(i);

            int id = movieJson.getInt(MDB_ID);
            String title = movieJson.getString(MDB_TITLE);
            String posterPath = movieJson.getString(MDB_POSTER_PATH);
            String voteAverage = movieJson.getString(MDB_VOTE_AVERAGE);
            String releaseDate = movieJson.getString(MDB_RELEASE_DATE);
            String overview = movieJson.getString(MDB_OVERVIEW);

            String posterUrl = MOVIE_POSTER_BASE_URL + posterPath;

            TMDBMovie movie = new TMDBMovie(id, title, releaseDate, posterUrl, voteAverage, overview);
            resultsList.add(movie);
        }

        return resultsList;
    }

    public static List<TMDBVideo> getVideosFromJson(String movieDataJsonStr) throws JSONException {
        // These are the names of the JSON objects that need to be extracted.
        final String MDB_LIST = "results";
        final String MDB_KEY = "key";
        final String MDB_NAME = "name";

        JSONObject moviesJson = new JSONObject(movieDataJsonStr);
        JSONArray movieResultsArray = moviesJson.getJSONArray(MDB_LIST);

        List<TMDBVideo> resultsList = new ArrayList<>();

        for (int i = 0; i < movieResultsArray.length(); i++) {

            JSONObject movieJson = movieResultsArray.getJSONObject(i);

            String key = movieJson.getString(MDB_KEY);
            String name = movieJson.getString(MDB_NAME);

            TMDBVideo trailer = new TMDBVideo(name, key);
            resultsList.add(trailer);
        }

        return resultsList;
    }

    public static List<TMDBReview> getReviewsMoviesFromJson(String movieDataJsonStr) throws JSONException {
        // These are the names of the JSON objects that need to be extracted.
        final String MDB_LIST = "results";
        final String MDB_AUTHOR = "author";
        final String MDB_CONTENT = "content";

        JSONObject moviesJson = new JSONObject(movieDataJsonStr);
        JSONArray movieResultsArray = moviesJson.getJSONArray(MDB_LIST);

        List<TMDBReview> resultsList = new ArrayList<>();

        for (int i = 0; i < movieResultsArray.length(); i++) {

            JSONObject movieJson = movieResultsArray.getJSONObject(i);

            String author = movieJson.getString(MDB_AUTHOR);
            String content = movieJson.getString(MDB_CONTENT);

            TMDBReview review = new TMDBReview(author, content);
            resultsList.add(review);
        }

        return resultsList;
    }
}
