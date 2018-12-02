package com.spundev.popularmovies;


import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.spundev.popularmovies.databinding.ActivitySettingsBinding;
import com.spundev.popularmovies.util.Theme;

/**
 * A {@link PreferenceActivity} that presents a set of application settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Data binding
        ActivitySettingsBinding binding =
                DataBindingUtil.setContentView(this, R.layout.activity_settings);

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
        }
    }
}