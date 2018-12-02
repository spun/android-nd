package com.spundev.popularmovies;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.spundev.popularmovies.databinding.ActivityDetailBinding;
import com.spundev.popularmovies.model.TMDBMovie;
import com.spundev.popularmovies.util.Theme;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Data binding
        ActivityDetailBinding binding =
                DataBindingUtil.setContentView(this, R.layout.activity_detail);

        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);

        // Toolbar back arrow
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (savedInstanceState == null) {
            // Apply the selected to the current activity
            Theme.applyTheme(this);

            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Intent intent = getIntent();
            TMDBMovie movieDetail = intent.getParcelableExtra(Intent.EXTRA_TEXT);

            Bundle args = new Bundle();
            args.putParcelable(DetailActivityFragment.DETAIL_DATA, movieDetail);

            DetailActivityFragment fragment = new DetailActivityFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.detail_container, fragment)
                    .commit();
        }
    }
}
