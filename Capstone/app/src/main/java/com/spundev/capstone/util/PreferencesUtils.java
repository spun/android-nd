package com.spundev.capstone.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;

import com.spundev.capstone.R;


public class PreferencesUtils {

    // Get selected output language
    public static String getPreferenceLanguage(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        String languageKey = context.getString(R.string.pref_output_language_key);
        String languageDefault = context.getString(R.string.pref_output_language_option_default);

        return sp.getString(languageKey, languageDefault);
    }

    public static Boolean getPreferenceNearMe(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        String preferenceKey = context.getString(R.string.pref_near_me_key);

        return sp.getBoolean(preferenceKey, true);
    }
}
