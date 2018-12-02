package com.spundev.capstone.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.spundev.capstone.R;

import java.util.Arrays;
import java.util.List;

public class MyAccountActivity extends AppCompatActivity {

    private static final String TAG = "MyAccountActivity";

    private LinearLayout signInScreen;
    private LinearLayout userInfoScreen;

    private ImageView userPhotoImageView;
    private TextView userNameTextView;

    private static final int RC_SIGN_IN = 100;

    public static void start(Context context) {
        Intent starter = new Intent(context, MyAccountActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_account);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Toolbar back arrow
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }

        signInScreen = findViewById(R.id.account_sign_in_screen);
        userInfoScreen = findViewById(R.id.account_user_info_screen);

        // User info
        userPhotoImageView = findViewById(R.id.account_user_photo);
        userNameTextView = findViewById(R.id.account_user_name);

        initListeners();
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // User is signed in
            showUserInfoScreen(user);
        } else {
            // No user is signed in
            showSignInScreen();
        }
    }

    private void showSignInScreen() {
        // Show correct screen
        signInScreen.setVisibility(View.VISIBLE);
        userInfoScreen.setVisibility(View.GONE);
    }


    private void showUserInfoScreen(FirebaseUser user) {

        // Show correct screen
        signInScreen.setVisibility(View.GONE);
        userInfoScreen.setVisibility(View.VISIBLE);

        // Name and profile photo Url
        String name = user.getDisplayName();
        Uri photoUrl = user.getPhotoUrl();

        // Show user name
        userNameTextView.setText(name);
        // Show user photo
        Glide.with(this).load(photoUrl).into(userPhotoImageView);
    }


    private void initListeners() {
        // Firebase Auth button
        Button authButton = findViewById(R.id.auth_button);
        authButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Choose authentication providers
                List<AuthUI.IdpConfig> providers = Arrays.asList(
                        new AuthUI.IdpConfig.EmailBuilder().build(),
                        new AuthUI.IdpConfig.GoogleBuilder().build());

                // Create and launch sign-in intent
                startActivityForResult(
                        AuthUI.getInstance()
                                .createSignInIntentBuilder()
                                .setAvailableProviders(providers)
                                .setLogo(R.drawable.ic_logo_green_196dp)      // Set logo drawable
                                .build(),
                        RC_SIGN_IN);
            }
        });

        // Cancel Auth button
        Button cancelButton = findViewById(R.id.auth_cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // Cancel Auth button
        Button signOut = findViewById(R.id.auth_sign_out_button);
        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(MyAccountActivity.this, R.string.account_session_closed, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // RC_SIGN_IN is the request code you passed into startActivityForResult(...) when starting the sign in flow.
        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            // Successfully signed in
            if (resultCode == RESULT_OK) {
                //startActivity(SignedInActivity.createIntent(this, response));
                Toast.makeText(MyAccountActivity.this, R.string.account_session_open, Toast.LENGTH_SHORT).show();
                finish();
            } else {
                // Sign in failed
                if (response == null) {
                    // User pressed back button
                    // showSnackbar(R.string.sign_in_cancelled);
                    // Toast.makeText(MyAccountActivity.this, "cancelled", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
                    Toast.makeText(MyAccountActivity.this, R.string.account_error_no_connection, Toast.LENGTH_SHORT).show();
                    // showSnackbar(R.string.no_internet_connection);
                    return;
                }

                Toast.makeText(MyAccountActivity.this, R.string.account_auth_error, Toast.LENGTH_SHORT).show();
                //showSnackbar(R.string.unknown_error);
                //Log.e(TAG, "Sign-in error: ", response.getError());
            }
        }
    }
}
