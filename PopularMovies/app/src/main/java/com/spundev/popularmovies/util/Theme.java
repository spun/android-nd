package com.spundev.popularmovies.util;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.preference.PreferenceManager;

import com.spundev.popularmovies.R;

/**
 * Created by spundev.
 */

public class Theme {

    // Apply the selected theme from settings to an activity
    public static void applyTheme(AppCompatActivity activity) {
        // Theme preferences
        String keyForTheme = activity.getString(R.string.pref_theme_key);
        String lightTheme = activity.getString(R.string.pref_theme_light);
        String darkTheme = activity.getString(R.string.pref_theme_dark);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(activity);
        String value = sp.getString(keyForTheme, lightTheme);

        // Apply (no activity recreation needed since 24.1.0)
        if (value.equals(darkTheme)) {
            activity.getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            activity.getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
}
