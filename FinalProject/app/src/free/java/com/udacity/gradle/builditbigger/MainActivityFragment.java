package com.udacity.gradle.builditbigger;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.spundev.jokesandroidlibrary.JokeActivity;


/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    private Button tellJokeButton;
    private ProgressBar jokeProgressBar;

    private EndpointsAsyncTask endpointsAsyncTask;

    private InterstitialAd mInterstitialAd;

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_main, container, false);

        AdView mAdView = root.findViewById(R.id.adView);
        // Create an ad request. Check logcat output for the hashed device ID to
        // get test ads on a physical device. e.g.
        // "Use AdRequest.Builder.addTestDevice("ABCDEF012345") to get test ads on this device."
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();
        mAdView.loadAd(adRequest);

        // UI elements
        tellJokeButton = root.findViewById(R.id.tellJoke_button);
        jokeProgressBar = root.findViewById(R.id.joke_progressBar);

        prepareInterstitialAd();
        initListeners();

        return root;
    }


    private void initListeners() {
        // When the tell joke is clicked
        tellJokeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mInterstitialAd.isLoaded()) {
                    // Show the ad
                    mInterstitialAd.show();
                } else {
                    // The interstitial wasn't loaded yet, show the joke
                    showJokeActivity();
                }
            }
        });
    }


    private void prepareInterstitialAd() {
        // https://developers.google.com/admob/android/interstitial
        MobileAds.initialize(getActivity(), getString(R.string.ad_mob_app_code));
        mInterstitialAd = new InterstitialAd(getActivity());
        mInterstitialAd.setAdUnitId(getString(R.string.interstitial_ad_unit_id));

        // InterstitialAd listeners
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(int errorCode) {
                // Code to be executed when an ad request fails.
                // Show the joke If we can't show the ad
                showJokeActivity();
            }

            @Override
            public void onAdClosed() {
                // Code to be executed when when the interstitial ad is closed.
                // Load the next interstitial.
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
                showJokeActivity();
            }
        });
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
    }


    private void showProgressBar(boolean show) {
        // toggle progress bar
        if (show) {
            jokeProgressBar.setVisibility(View.VISIBLE);
        } else {
            jokeProgressBar.setVisibility(View.GONE);
        }
    }

    private void showJokeActivity() {

        // Get joke from endpoint
        endpointsAsyncTask = new EndpointsAsyncTask() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                showProgressBar(true);
            }

            @Override
            protected void onPostExecute(String joke) {
                super.onPostExecute(joke);
                showProgressBar(false);
                if (joke != null) {
                    JokeActivity.start(getActivity(), joke);
                } else {
                    Toast.makeText(getActivity(), R.string.invalid_joke_error, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            protected void onCancelled(String s) {
                showProgressBar(false);
                super.onCancelled(s);
            }
        };
        endpointsAsyncTask.execute();
    }


    @Override
    public void onPause() {
        super.onPause();
        // Cancel the job if we leave the activity
        if (endpointsAsyncTask != null) {
            endpointsAsyncTask.cancel(true);
        }
    }
}
