package com.udacity.gradle.builditbigger;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.spundev.jokesandroidlibrary.JokeActivity;


/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    private Button tellJokeButton;
    private ProgressBar jokeProgressBar;

    private EndpointsAsyncTask endpointsAsyncTask;

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_main, container, false);

        // UI elements
        tellJokeButton = root.findViewById(R.id.tellJoke_button);
        jokeProgressBar = root.findViewById(R.id.joke_progressBar);

        initListeners();

        return root;
    }

    private void initListeners() {
        // When the tell joke is clicked
        tellJokeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // show the joke
                showJokeActivity();
            }
        });
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
