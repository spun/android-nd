package com.spundev.capstone;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;

import com.spundev.capstone.ui.MainActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if this is the first time the user opens the app and show the main
        // activity or the onboarding screen
        // startActivity(new Intent(SplashActivity.this, WelcomeActivity.class));
        // TODO

        // Start home activity
        //startActivity(new Intent(SplashActivity.this, MainActivity.class));
        startActivity(new Intent(SplashActivity.this, MainActivity.class), ActivityOptionsCompat.makeSceneTransitionAnimation(SplashActivity.this).toBundle());



        // close splash activity
        finish();
    }
}
