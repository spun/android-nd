package com.spundev.capstone;

import android.support.multidex.MultiDexApplication;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.analytics.FirebaseAnalytics;

import io.fabric.sdk.android.Fabric;

@SuppressWarnings("WeakerAccess")
public class CapstoneApplication extends MultiDexApplication {
    @Override
    public void onCreate() {
        super.onCreate();

        // Enable analytics and crash reporting only if is a release build
        if (!BuildConfig.DEBUG) {
            // Enable crash reporting
            Fabric.with(this, new Crashlytics());

            // Enable analytics
            FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(this);
            firebaseAnalytics.setAnalyticsCollectionEnabled(true);
        }
    }
}
