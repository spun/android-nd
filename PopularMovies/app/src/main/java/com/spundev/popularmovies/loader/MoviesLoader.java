package com.spundev.popularmovies.loader;

import android.content.Context;
import android.net.Uri;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.spundev.popularmovies.BuildConfig;
import com.spundev.popularmovies.model.TMDBMovie;
import com.spundev.popularmovies.util.ApiFetch;
import com.spundev.popularmovies.util.TMDBApi;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by spundev.
 */

public class MoviesLoader extends AsyncTaskLoader<List<TMDBMovie>> {

    private static final String TAG = "MoviesLoader";
    public static final int SORT_ORDER_POPULAR = 0;
    public static final int SORT_ORDER_RATED = 1;

    @SuppressWarnings("FieldCanBeLocal")
    private final String MDB_TOP_RATED_BASE_URL = "https://api.themoviedb.org/3/movie/top_rated";
    @SuppressWarnings("FieldCanBeLocal")
    private final String MDB_POPULAR_API_BASE_URL = "https://api.themoviedb.org/3/movie/popular";

    private List<TMDBMovie> mData;
    private final int mSortOrder;

    public MoviesLoader(Context context, int sortOrder) {
        super(context);
        // This indicates if we were asked for the popular movies
        // or for the top rated movies.
        mSortOrder = sortOrder;
    }

    @Override
    protected void onStartLoading() {
        if (mData != null) {
            // Use cached data
            deliverResult(mData);
        } else {
            // We have no data, so kick off loading it
            forceLoad();
        }
    }

    @Override
    public List<TMDBMovie> loadInBackground() {
        // This is on a background thread
        return fetchData();
    }

    private List<TMDBMovie> fetchData() {

        String apiBaseUrl = MDB_POPULAR_API_BASE_URL;
        if (mSortOrder == SORT_ORDER_RATED) {
            apiBaseUrl = MDB_TOP_RATED_BASE_URL;
        }

        Uri builtUri = Uri.parse(apiBaseUrl).buildUpon()
                .appendQueryParameter("api_key", BuildConfig.MOVIE_DB_API_KEY)
                .build();

        try {
            String movieDataJsonStr = ApiFetch.fetchJson(builtUri);
            return TMDBApi.getMoviesFromJson(movieDataJsonStr);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }

        return new ArrayList<>();
    }


    @Override
    public void deliverResult(List<TMDBMovie> data) {
        // We’ll save the data for later retrieval
        mData = data;
        // We can do any pre-processing we want here
        // Just remember this is on the UI thread so nothing lengthy!
        super.deliverResult(data);
    }
}
