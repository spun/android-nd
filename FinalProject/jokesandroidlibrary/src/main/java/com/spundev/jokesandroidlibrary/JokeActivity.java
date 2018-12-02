package com.spundev.jokesandroidlibrary;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;

public class JokeActivity extends AppCompatActivity {

    private static final String JOKE_TEXT_EXTRA = "JOKE_TEXT_EXTRA";

    public static void start(Context context, String jokeText) {
        Intent starter = new Intent(context, JokeActivity.class);
        starter.putExtra(JOKE_TEXT_EXTRA, jokeText);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_joke);

        // Toolbar back arrow and title
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Get joke from intent
        Intent intent = getIntent();
        String jokeText = intent.getStringExtra(JOKE_TEXT_EXTRA);

        // set text
        TextView jokeTextView = findViewById(R.id.joke_textView);
        jokeTextView.setText(jokeText);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // We are not defining the parent of this
                // activity in the manifest. We need to manually
                // go back when the arrow is pressed.
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
