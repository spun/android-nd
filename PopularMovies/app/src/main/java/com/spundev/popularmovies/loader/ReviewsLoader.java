package com.spundev.popularmovies.loader;

import android.content.Context;
import android.net.Uri;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.spundev.popularmovies.BuildConfig;
import com.spundev.popularmovies.model.TMDBReview;
import com.spundev.popularmovies.util.ApiFetch;
import com.spundev.popularmovies.util.TMDBApi;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by spundev.
 */

public class ReviewsLoader extends AsyncTaskLoader<List<TMDBReview>> {

    private static final String TAG = "ReviewsLoader";

    private List<TMDBReview> mData;
    private final int mMovieId;

    public ReviewsLoader(Context context, int movieId) {
        super(context);
        mMovieId = movieId;
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
    public List<TMDBReview> loadInBackground() {
        // This is on a background thread
        return fetchMovieData();
    }


    private List<TMDBReview> fetchMovieData() {

        String apiBaseUrl = "https://api.themoviedb.org/3/movie/" + mMovieId + "/reviews";

        Uri builtUri = Uri.parse(apiBaseUrl).buildUpon()
                .appendQueryParameter("api_key", BuildConfig.MOVIE_DB_API_KEY)
                .build();

        try {
            String jsonDataStr = ApiFetch.fetchJson(builtUri);
            return TMDBApi.getReviewsMoviesFromJson(jsonDataStr);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }

        return new ArrayList<>();
    }


    @Override
    public void deliverResult(List<TMDBReview> data) {
        // We’ll save the data for later retrieval
        mData = data;
        // We can do any pre-processing we want here
        // Just remember this is on the UI thread so nothing lengthy!
        super.deliverResult(data);
    }

}

