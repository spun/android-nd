package com.spundev.bakingtime;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.spundev.bakingtime.databinding.FragmentStepDetailBinding;
import com.spundev.bakingtime.model.Step;
import com.spundev.bakingtime.provider.DatabaseContract;

import java.util.ArrayList;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class StepDetailActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "StepDetailFragment";

    // Loader id
    private static final int STEP_DETAIL_LOADER = 40;

    // Bundle arguments for selected recipe id and selected step
    public static final String SELECTED_RECIPE_EXTRA = "SELECTED_RECIPE_EXTRA";
    public static final String SELECTED_STEP_EXTRA = "SELECTED_STEP_EXTRA";
    private int selectedRecipeId;
    private int selectedStepId;

    // Columns projection
    private static final String[] STEP_COLUMNS = {
            DatabaseContract.StepEntry._ID,
            DatabaseContract.StepEntry.COLUMN_SHORT_DESCRIPTION,
            DatabaseContract.StepEntry.COLUMN_DESCRIPTION,
            DatabaseContract.StepEntry.COLUMN_VIDEO_URL,
            DatabaseContract.StepEntry.COLUMN_THUMBNAIL_URL
    };
    // These indices are tied to STEP_COLUMNS. If STEP_COLUMNS changes, these must change.
    private static final int COL_STEP_ID = 0;
    private static final int COL_STEP_SHORT_DESCRIPTION = 1;
    private static final int COL_STEP_DESCRIPTION = 2;
    private static final int COL_STEP_VIDEO_URL = 3;
    private static final int COL_STEP_THUMBNAIL_URL = 4;

    // Save current step onSaveInstanceState
    private static final String SAVED_STEP_ID = "SAVED_STEP_ID";
    // Save exo player state onSaveInstanceState
    private final String STATE_RESUME_PLAY_WHEN_READY = "resumePlayWhenReady";
    private final String STATE_RESUME_WINDOW = "resumeWindow";
    private final String STATE_RESUME_POSITION = "resumePosition";
    private final String STATE_PLAYER_FULLSCREEN = "playerFullscreen";

    // MediaSession
    private static MediaSessionCompat mMediaSession;
    private PlaybackStateCompat.Builder mStateBuilder;

    // Exo player
    private SimpleExoPlayerView mPlayerView;
    private boolean mPlayWhenReady = true;
    private int mResumeWindow;
    private long mResumePosition;
    private boolean mExoPlayerFullscreen = false;
    private Dialog mFullScreenDialog;
    ImageView mFullScreenIcon;

    private final List<Step> stepsList = new ArrayList<>();
    private int stepPosition;

    // Data binding
    private FragmentStepDetailBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Data binding
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_step_detail, container, false);

        // retrieve the id of the selected recipe and the selected step from the bundle
        Bundle arguments = getArguments();
        if (arguments != null) {
            selectedRecipeId = arguments.getInt(SELECTED_RECIPE_EXTRA);
            selectedStepId = arguments.getInt(SELECTED_STEP_EXTRA);
        }

        // on some layouts, we launch ExoPlayer in full screen
        boolean fullscreenLandscape = getResources().getBoolean(R.bool.fullscreen_landscape);
        if (fullscreenLandscape) {
            mExoPlayerFullscreen = true;
        }

        // Retrieve old ExoPlayer state
        if (savedInstanceState != null) {
            selectedStepId = savedInstanceState.getInt(SAVED_STEP_ID);
            mPlayWhenReady = savedInstanceState.getBoolean(STATE_RESUME_PLAY_WHEN_READY);
            mResumeWindow = savedInstanceState.getInt(STATE_RESUME_WINDOW);
            mResumePosition = savedInstanceState.getLong(STATE_RESUME_POSITION);
        }

        // Next step button listener
        binding.goNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check if we have any step
                if (stepPosition < stepsList.size() - 1) {
                    // Show step info
                    stepPosition = stepPosition + 1;
                    binding.setStepPosition(stepPosition);
                    binding.setStep(stepsList.get(stepPosition));
                    selectedStepId = stepsList.get(stepPosition).getId();
                    // Reset player position al initialize
                    mResumePosition = 0;
                    initExoPlayer(stepsList.get(stepPosition).getVideoURL());
                }
            }
        });
        // Previous step button listener
        binding.goBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check if we are in the first step
                if (stepPosition > 0) {
                    // Show step info
                    stepPosition = stepPosition - 1;
                    binding.setStepPosition(stepPosition);
                    binding.setStep(stepsList.get(stepPosition));
                    selectedStepId = stepsList.get(stepPosition).getId();
                    // Reset player position al initialize
                    mResumePosition = 0;
                    initExoPlayer(stepsList.get(stepPosition).getVideoURL());
                }
            }
        });

        // Full screen button listener
        FrameLayout mFullScreenButton = binding.playerView.findViewById(R.id.exo_fullscreen_button);
        mFullScreenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mExoPlayerFullscreen)
                    openFullscreenDialog();
                else
                    closeFullscreenDialog();
            }
        });

        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(STEP_DETAIL_LOADER, null, this);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt(SAVED_STEP_ID, selectedStepId);
        outState.putBoolean(STATE_RESUME_PLAY_WHEN_READY, mPlayWhenReady);
        outState.putInt(STATE_RESUME_WINDOW, mResumeWindow);
        outState.putLong(STATE_RESUME_POSITION, mResumePosition);
        outState.putBoolean(STATE_PLAYER_FULLSCREEN, mExoPlayerFullscreen);

        super.onSaveInstanceState(outState);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        if (id == STEP_DETAIL_LOADER) {
            // Uri for an individual step
            Uri savedMoviesUri = DatabaseContract.StepEntry.buildStepsFromRecipeUri(selectedRecipeId);

            // Return cursor loader
            return new CursorLoader(getActivity(),
                    savedMoviesUri,
                    STEP_COLUMNS,
                    null,
                    null,
                    null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        int loaderId = loader.getId();
        if (loaderId == STEP_DETAIL_LOADER) {

            stepsList.clear();
            int position = 0;
            if (data.moveToFirst()) {
                do {
                    // Retrieve step data
                    int id = data.getInt(COL_STEP_ID);
                    String shortDescription = data.getString(COL_STEP_SHORT_DESCRIPTION);
                    String description = data.getString(COL_STEP_DESCRIPTION);
                    String videoURL = data.getString(COL_STEP_VIDEO_URL);
                    String thumbnailURL = data.getString(COL_STEP_THUMBNAIL_URL);
                    // add step to the list
                    stepsList.add(new Step(id, shortDescription, description, videoURL, thumbnailURL));

                    // if the current step is the selected step, we save the
                    // position on our steps list
                    if (id == selectedStepId) {
                        stepPosition = position;
                    }
                    position++;
                } while (data.moveToNext());
            }

            // Update recipe progress bar
            // (the progress bar doesn't update correctly if we
            // don't do this)
            binding.stepsProgressBar.setMax(stepsList.size());
            binding.stepsProgressBar.setProgress(stepPosition);

            binding.setStepsTotal(stepsList.size());
            binding.setStepPosition(stepPosition);
            binding.setStep(stepsList.get(stepPosition));

            // Initialize the Media Session.
            initMediaSession();

            // Update player
            initExoPlayer(stepsList.get(stepPosition).getVideoURL());
        }
    }


    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }


    // https://github.com/google/ExoPlayer/blob/release-v2/demos/main/src/main/java/com/google/android/exoplayer2/demo/PlayerActivity.java#L186
    @Override
    public void onPause() {
        super.onPause();
        // We update this values here in case onSaveInstances is called to save the values
        // after we release the player
        if (mPlayerView != null && mPlayerView.getPlayer() != null) {
            mResumeWindow = mPlayerView.getPlayer().getCurrentWindowIndex();
            mResumePosition = Math.max(0, mPlayerView.getPlayer().getContentPosition());
            mPlayWhenReady = mPlayerView.getPlayer().getPlayWhenReady();
        }

        // releasePlayer
        if (Build.VERSION.SDK_INT <= 23) {
            releasePlayer();
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        // releasePlayer
        if (Build.VERSION.SDK_INT > 23) {
            releasePlayer();
        }
    }


    private void initMediaSession() {
        // Create a MediaSessionCompat.
        mMediaSession = new MediaSessionCompat(getActivity(), TAG);

        // Enable callbacks from MediaButtons and TransportControls.
        mMediaSession.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        // Do not let MediaButtons restart the player when the app is not visible.
        mMediaSession.setMediaButtonReceiver(null);

        // Set an initial PlaybackState with ACTION_PLAY, so media buttons can start the player.
        mStateBuilder = new PlaybackStateCompat.Builder()
                .setActions(
                        PlaybackStateCompat.ACTION_PLAY |
                                PlaybackStateCompat.ACTION_PAUSE |
                                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                                PlaybackStateCompat.ACTION_PLAY_PAUSE);

        mMediaSession.setPlaybackState(mStateBuilder.build());


        // MySessionCallback has methods that handle callbacks from a media controller.
        mMediaSession.setCallback(new MySessionCallback());

        // Start the Media Session since the activity is active.
        mMediaSession.setActive(true);
    }


    private void initExoPlayer(String uri) {

        if (mPlayerView == null || mPlayerView.getPlayer() == null) {
            mPlayerView = binding.playerView;
            // Create an instance of the ExoPlayer.
            final SimpleExoPlayer player = ExoPlayerFactory.newSimpleInstance(new DefaultRenderersFactory(getActivity()), new DefaultTrackSelector(), new DefaultLoadControl());
            mPlayerView.setPlayer(player);

            // Audio manager (for focus)
            final AudioManager audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);

            // Noisy Intent
            final IntentFilter noisyIntentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);

            // Set the ExoPlayer.EventListener
            player.addListener(new Player.DefaultEventListener() {
                @Override
                public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                    super.onPlayerStateChanged(playWhenReady, playbackState);

                    if ((playbackState == Player.STATE_READY) && playWhenReady) {     // PLAY
                        // Request audio focus
                        int result = audioManager.requestAudioFocus(
                                mOnAudioFocusChangeListener,
                                AudioManager.STREAM_MUSIC,
                                AudioManager.AUDIOFOCUS_GAIN);

                        // Check if we got the focus
                        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                            mStateBuilder.setState(PlaybackStateCompat.STATE_PLAYING,
                                    mPlayerView.getPlayer().getCurrentPosition(), 1f);
                        } else {
                            // If we didn't get the focus (maybe we are in a call) we pause immediately
                            player.setPlayWhenReady(false);
                        }

                        // Noisy receiver (for when we remove headphones)
                        getActivity().registerReceiver(mNoisyReceiver, noisyIntentFilter);

                    } else if (playbackState == Player.STATE_READY) {   // PAUSE
                        // Release focus
                        audioManager.abandonAudioFocus(mOnAudioFocusChangeListener);

                        mStateBuilder.setState(PlaybackStateCompat.STATE_PAUSED,
                                mPlayerView.getPlayer().getCurrentPosition(), 1f);

                        try {
                            getActivity().unregisterReceiver(mNoisyReceiver);
                        } catch (Exception e) {
                            Log.e(TAG, "onPlayerStateChanged: Trying to unregister a non registered receiver");
                        }

                    } else if (playbackState == Player.STATE_ENDED) {   // STOP
                        // Release focus
                        audioManager.abandonAudioFocus(mOnAudioFocusChangeListener);

                        mStateBuilder.setState(PlaybackStateCompat.STATE_STOPPED,
                                mPlayerView.getPlayer().getCurrentPosition(), 1f);

                        try {
                            getActivity().unregisterReceiver(mNoisyReceiver);
                        } catch (Exception e) {
                            Log.e(TAG, "onPlayerStateChanged: Trying to unregister a non registered receiver");
                        }
                    }

                    mMediaSession.setPlaybackState(mStateBuilder.build());
                }
            });

            initFullscreenDialog();
        } else {
            mPlayerView.getPlayer().stop();
            mPlayerView.getPlayer().seekTo(0);
        }

        // If step video uri is valid
        if (!TextUtils.isEmpty(uri)) {
            binding.mainMediaFrame.setVisibility(View.VISIBLE);
            boolean haveResumePosition = mResumeWindow != C.INDEX_UNSET;
            if (haveResumePosition) {
                mPlayerView.getPlayer().seekTo(mResumeWindow, mResumePosition);
            }
            mPlayerView.getPlayer().setPlayWhenReady(mPlayWhenReady);

            MediaSource mediaSource = new ExtractorMediaSource.Factory(new DefaultHttpDataSourceFactory("bakingtime")).createMediaSource(Uri.parse(uri));
            mPlayerView.getPlayer().prepare(mediaSource, false, true);

            if (mExoPlayerFullscreen) {
                openFullscreenDialog();
            }

        } else {
            binding.mainMediaFrame.setVisibility(View.GONE);
        }
    }

    // FocusChangeListener
    AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS ||
                    focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                if (mPlayerView != null && mPlayerView.getPlayer() != null) {
                    mPlayerView.getPlayer().setPlayWhenReady(false);
                }
            }
        }
    };

    // NoisyReceiver
    private BroadcastReceiver mNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Always pause the video
            mPlayerView.getPlayer().setPlayWhenReady(false);
        }
    };

    private void releasePlayer() {

        try {
            // Media session
            mMediaSession.setActive(false);
            // Focus
            final AudioManager audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
            audioManager.abandonAudioFocus(mOnAudioFocusChangeListener);
            // Noisy receiver
            getActivity().unregisterReceiver(mNoisyReceiver);
        } catch (Exception e) {
            Log.e(TAG, "onPlayerStateChanged: Trying to unregister a non registered receiver");
        }

        if (mFullScreenDialog != null)
            mFullScreenDialog.dismiss();

        SimpleExoPlayer mExoPlayer = mPlayerView.getPlayer();
        if (mExoPlayer != null) {
            mExoPlayer.stop();
            mExoPlayer.release();
        }
        mPlayerView = null;
    }

    private void initFullscreenDialog() {

        mFullScreenDialog = new Dialog(getActivity(), android.R.style.Theme_Black_NoTitleBar_Fullscreen) {
            public void onBackPressed() {
                if (mExoPlayerFullscreen)
                    closeFullscreenDialog();
                super.onBackPressed();
            }
        };
    }

    // open fullscreen
    private void openFullscreenDialog() {
        ((ViewGroup) mPlayerView.getParent()).removeView(mPlayerView);
        mFullScreenDialog.addContentView(mPlayerView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mExoPlayerFullscreen = true;
        mFullScreenDialog.show();

    }

    // close fullscreen
    private void closeFullscreenDialog() {
        ((ViewGroup) mPlayerView.getParent()).removeView(mPlayerView);
        binding.mainMediaFrame.addView(mPlayerView);
        mExoPlayerFullscreen = false;
        mFullScreenDialog.dismiss();

    }

    private class MySessionCallback extends MediaSessionCompat.Callback {

        @Override
        public void onPlay() {
            mPlayerView.getPlayer().setPlayWhenReady(true);
        }

        @Override
        public void onPause() {
            mPlayerView.getPlayer().setPlayWhenReady(false);
        }
    }

    /**
     * Broadcast Receiver registered to receive the MEDIA_BUTTON intent coming from clients.
     */
    public static class MediaReceiver extends BroadcastReceiver {

        public MediaReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            MediaButtonReceiver.handleIntent(mMediaSession, intent);
        }
    }
}
