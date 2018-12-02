package com.spundev.popularmovies;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.SnapHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.spundev.popularmovies.adapter.ReviewsAdapter;
import com.spundev.popularmovies.adapter.VideosAdapter;
import com.spundev.popularmovies.data.DatabaseContract;
import com.spundev.popularmovies.data.DatabaseContract.MovieEntry;
import com.spundev.popularmovies.databinding.FragmentDetailBinding;
import com.spundev.popularmovies.loader.ReviewsLoader;
import com.spundev.popularmovies.loader.VideosLoader;
import com.spundev.popularmovies.model.TMDBMovie;
import com.spundev.popularmovies.model.TMDBReview;
import com.spundev.popularmovies.model.TMDBVideo;
import com.spundev.popularmovies.util.Youtube;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * A fragment containing movie details.
 */

public class DetailActivityFragment extends Fragment {

    private static final String TAG = "DetailActivityFragment";

    public static final String DETAIL_DATA = "DETAIL";

    // Loaders ids
    private static final int FAVORITE_STATUS_LOADER = 10;
    private static final int VIDEOS_LOADER = 20;
    private static final int REVIEWS_LOADER = 30;

    // Movie data
    private TMDBMovie mMovieDetail;

    // Favorite button (two states)
    private Button mAddFavoriteButton;
    private Button mRemoveFavoriteButton;
    private Toast mFavoriteToast;

    // Videos section elements
    private VideosAdapter mVideosAdapter;
    private ProgressBar mVideosProgressBar;
    private TextView mVideosEmptyTextView;

    // Reviews section elements
    private ReviewsAdapter mReviewsAdapter;
    private ProgressBar mReviewsProgressBar;
    private TextView mReviewsEmptyTextView;

    private String shareableVideo = "";

    // Columns projections. Used to check if the movie is already in favorites
    // and show the appropriated button
    private static final String[] MOVIE_COLUMNS = {
            // We will check if the id of the current movie is in the db
            DatabaseContract.MovieEntry._ID
    };

    // event handlers
    public class DetailFragmentHandlers {
        public void onClickAddToFavorites(@SuppressWarnings("unused") View view) {
            try {
                // add to favorites database
                ContentValues mNewMovieValues = new ContentValues();
                mNewMovieValues.put(MovieEntry._ID, mMovieDetail.getId());
                mNewMovieValues.put(MovieEntry.COLUMN_TITLE, mMovieDetail.getTitle());
                mNewMovieValues.put(MovieEntry.COLUMN_RELEASE_DATE, mMovieDetail.getReleaseDate());
                mNewMovieValues.put(MovieEntry.COLUMN_POSTER_URL, mMovieDetail.getPosterUrl());
                mNewMovieValues.put(MovieEntry.COLUMN_VOTE_AVERAGE, mMovieDetail.getVoteAverage());
                mNewMovieValues.put(MovieEntry.COLUMN_OVERVIEW, mMovieDetail.getOverview());

                getActivity().getContentResolver().insert(MovieEntry.CONTENT_URI, mNewMovieValues);

                // Show confirmation message via toast
                if (mFavoriteToast != null) {
                    mFavoriteToast.cancel();
                }
                mFavoriteToast = Toast.makeText(getActivity(), R.string.add_to_favorites_done, Toast.LENGTH_SHORT);
                mFavoriteToast.show();
            } catch (SQLException ex) {
                Log.e(TAG, "onClickAddToFavorites: ", ex);
            }
        }

        public void onClickRemoveFromFavorites(@SuppressWarnings("unused") View view) {

            try {
                // delete from database
                getActivity().getContentResolver().delete(DatabaseContract.MovieEntry.CONTENT_URI,
                        DatabaseContract.MovieEntry._ID + " = ?", new String[]{Integer.toString(mMovieDetail.getId())});

                // Show confirmation message via toast
                if (mFavoriteToast != null) {
                    mFavoriteToast.cancel();
                }
                mFavoriteToast = Toast.makeText(getActivity(), R.string.remove_from_favorites_done, Toast.LENGTH_SHORT);
                mFavoriteToast.show();
            } catch (SQLException ex) {
                Log.e(TAG, "onClickRemoveFromFavorites: ", ex);
            }
        }
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Data binding
        FragmentDetailBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_detail, container, false);
        Bundle arguments = getArguments();

        if (arguments != null) {
            // Set received movie data
            mMovieDetail = arguments.getParcelable(DetailActivityFragment.DETAIL_DATA);
            binding.movieData.setMovie(mMovieDetail);
            binding.setMovieSelected(mMovieDetail != null);

            // Movie poster
            ImageView posterImageView = binding.movieData.posterImageView;
            // Poster final url
            final String moviePosterUrl = mMovieDetail.getPosterUrl();
            Picasso.with(getContext())
                    .load(moviePosterUrl)
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
                    .into(posterImageView);

            // Videos RecyclerView
            binding.movieData.videosRecyclerView.setHasFixedSize(true);
            binding.movieData.videosRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
            binding.movieData.videosRecyclerView.setNestedScrollingEnabled(false);
            mVideosAdapter = new VideosAdapter(new VideosAdapter.VideoAdapterOnClickHandler() {
                @Override
                public void onClick(String videoKey) {
                    // Launch video on youtube
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Youtube.getVideoUrlFromKey(videoKey))));
                }
            });
            binding.movieData.videosRecyclerView.setAdapter(mVideosAdapter);

            // Snap on scroll
            SnapHelper snapHelper = new LinearSnapHelper();
            snapHelper.attachToRecyclerView(binding.movieData.videosRecyclerView);

            // Reviews RecyclerView
            binding.movieData.reviewsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            binding.movieData.reviewsRecyclerView.setNestedScrollingEnabled(false);
            mReviewsAdapter = new ReviewsAdapter();
            binding.movieData.reviewsRecyclerView.setAdapter(mReviewsAdapter);

            // Event handlers
            binding.movieData.setHandlers(new DetailFragmentHandlers());

            // ui elements used by other methods of the class
            mAddFavoriteButton = binding.movieData.addToFavoritesButton;
            mRemoveFavoriteButton = binding.movieData.removeFromFavoritesButton;

            mVideosProgressBar = binding.movieData.videosProgressBar;
            mVideosEmptyTextView = binding.movieData.videosEmptyTextView;

            mReviewsProgressBar = binding.movieData.reviewsProgressBar;
            mReviewsEmptyTextView = binding.movieData.reviewsEmptyTextView;
        }

        return binding.getRoot();
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // We use multiple loaders to populate the sections of the details ui.
        if (mMovieDetail != null) {

            // Check if movie is already added to favorites or not and show the right button.
            getLoaderManager().initLoader(FAVORITE_STATUS_LOADER, null, new LoaderManager.LoaderCallbacks<Cursor>() {

                @Override
                public Loader<Cursor> onCreateLoader(int id, Bundle args) {

                    if (id == FAVORITE_STATUS_LOADER) {
                        // This is called when a new Loader needs to be created.
                        Uri savedMoviesUri = DatabaseContract.MovieEntry.buildMovieUri(mMovieDetail.getId());

                        return new CursorLoader(getActivity(),
                                savedMoviesUri,
                                MOVIE_COLUMNS,
                                null,
                                null,
                                null);
                    }
                    return null;
                }


                @Override
                public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
                    int loaderId = loader.getId();
                    if (loaderId == FAVORITE_STATUS_LOADER) {
                        if (data.getCount() == 0) {
                            mAddFavoriteButton.setVisibility(View.VISIBLE);
                            mRemoveFavoriteButton.setVisibility(View.GONE);
                        } else {
                            mAddFavoriteButton.setVisibility(View.GONE);
                            mRemoveFavoriteButton.setVisibility(View.VISIBLE);
                        }
                    }
                }

                @Override
                public void onLoaderReset(Loader<Cursor> loader) {

                }
            });

            // Movie trailers loader
            getLoaderManager().initLoader(VIDEOS_LOADER, null, new LoaderManager.LoaderCallbacks<List<TMDBVideo>>() {
                @Override
                public Loader<List<TMDBVideo>> onCreateLoader(int id, Bundle args) {
                    if (id == VIDEOS_LOADER) {
                        mVideosProgressBar.setVisibility(View.VISIBLE);
                        mVideosEmptyTextView.setVisibility(View.GONE);
                        return new VideosLoader(getActivity(), mMovieDetail.getId());
                    }
                    return null;
                }

                @Override
                public void onLoadFinished(Loader<List<TMDBVideo>> loader, List<TMDBVideo> data) {
                    int loaderId = loader.getId();
                    if (loaderId == VIDEOS_LOADER) {
                        mVideosProgressBar.setVisibility(View.GONE);
                        if (data.size() == 0) {
                            mVideosEmptyTextView.setVisibility(View.VISIBLE);
                            setHasOptionsMenu(false);
                        } else {
                            mVideosEmptyTextView.setVisibility(View.GONE);
                            mVideosAdapter.setItems(data);
                            // We are adding the possibility to share the first video
                            // so we only so the menu if we had a video to share
                            shareableVideo = Youtube.getShareableVideoUrlFromKey(data.get(0).getKey());
                            setHasOptionsMenu(true);
                        }
                    }
                }

                @Override
                public void onLoaderReset(Loader<List<TMDBVideo>> loader) {

                }
            });

            // Movie reviews loader
            getLoaderManager().initLoader(REVIEWS_LOADER, null, new LoaderManager.LoaderCallbacks<List<TMDBReview>>() {
                @Override
                public Loader<List<TMDBReview>> onCreateLoader(int id, Bundle args) {
                    if (id == REVIEWS_LOADER) {
                        mReviewsProgressBar.setVisibility(View.VISIBLE);
                        mReviewsEmptyTextView.setVisibility(View.GONE);
                        return new ReviewsLoader(getActivity(), mMovieDetail.getId());
                    }
                    return null;
                }

                @Override
                public void onLoadFinished(Loader<List<TMDBReview>> loader, List<TMDBReview> data) {
                    int loaderId = loader.getId();
                    if (loaderId == REVIEWS_LOADER) {
                        mReviewsProgressBar.setVisibility(View.GONE);
                        if (data.size() == 0) {
                            mReviewsEmptyTextView.setVisibility(View.VISIBLE);
                        } else {
                            mReviewsEmptyTextView.setVisibility(View.GONE);
                            mReviewsAdapter.setItems(data);
                        }
                    }
                }

                @Override
                public void onLoaderReset(Loader<List<TMDBReview>> loader) {

                }
            });
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.detail, menu);
        MenuItem menuItem = menu.findItem(R.id.action_share);
        menuItem.setIntent(createShareVideoIntent());
    }

    private Intent createShareVideoIntent() {
        String shareRawMessage = getResources().getString(R.string.share_message);
        String shareFinalMessage = String.format(shareRawMessage, mMovieDetail.getTitle(), shareableVideo);

        return ShareCompat.IntentBuilder.from(getActivity())
                .setType("text/plain")
                .setText(shareFinalMessage)
                .getIntent();
    }
}
