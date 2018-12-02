package com.spundev.capstone.ui;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.spundev.capstone.R;
import com.spundev.capstone.viewmodel.TTSViewModel;

public class TTSActivity extends AppCompatActivity {

    public static final String TTS_TEXT_EXTRA = "TTS_TEXT_EXTRA";

    // Views
    private TextView ttsTextView;
    private Button playButton;
    private Button stopButton;
    private Button editButton;

    // view model
    private TTSViewModel ttsViewModel;

    public static void start(Context context, String text) {
        Intent starter = new Intent(context, TTSActivity.class);
        starter.putExtra(TTS_TEXT_EXTRA, text);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tts);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Toolbar back arrow
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Play an stop buttons
        playButton = findViewById(R.id.play_button);
        stopButton = findViewById(R.id.stop_button);
        // Edit button
        editButton = findViewById(R.id.edit_button);
        // Text view
        ttsTextView = findViewById(R.id.tts_textView);

        // Get a new or existing ViewModel from the ViewModelProvider.
        ttsViewModel = ViewModelProviders.of(this).get(TTSViewModel.class);

        // TextView value observer
        ttsViewModel.getTTSText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                ttsTextView.setText(s);
            }
        });

        // Play/stop button observer
        ttsViewModel.getIsPlaying().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean isPlaying) {
                if (isPlaying) {
                    playButton.setVisibility(View.GONE);
                    stopButton.setVisibility(View.VISIBLE);
                } else {
                    playButton.setVisibility(View.VISIBLE);
                    stopButton.setVisibility(View.GONE);
                }
            }
        });


        // Check if we have an initial text (via database or intent filter)
        // Get intent, action and MIME type
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            // From Intent filter
            if ("text/plain".equals(type)) {
                // Handle text being sent
                String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
                ttsViewModel.setTTSText(sharedText);
            }
        } else {
            // From card
            final String sharedText = intent.getStringExtra(TTS_TEXT_EXTRA);
            if (!sharedText.isEmpty()) {
                ttsViewModel.setTTSText(sharedText);
            } else {
                showChangeTextDialog();
            }
        }

        // Click listeners
        initListeners();
    }

    private void initListeners() {
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start audio
                ttsViewModel.startAudio();

            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Stop audio
                ttsViewModel.stopAudio();

            }
        });

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChangeTextDialog();
            }
        });
    }

    private void showChangeTextDialog() {
        // Set current text
        final EditText dialogTTSText = new EditText(TTSActivity.this);
        dialogTTSText.setText(ttsViewModel.getTTSText().getValue());

        // Add margin to the EditText (https://stackoverflow.com/a/27776276)
        FrameLayout container = new FrameLayout(this);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = (int) getResources().getDimension(R.dimen.dialog_margin);
        params.leftMargin = (int) getResources().getDimension(R.dimen.dialog_margin);
        params.rightMargin = (int) getResources().getDimension(R.dimen.dialog_margin);
        dialogTTSText.setLayoutParams(params);
        container.addView(dialogTTSText);

        // Show edit dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(TTSActivity.this);
        builder.setTitle(getString(R.string.tts_change_text_title));
        builder.setView(container);

        String positiveText = getString(android.R.string.ok);
        builder.setPositiveButton(positiveText,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // positive button logic
                        String newText = String.valueOf(dialogTTSText.getText());
                        ttsViewModel.setTTSText(newText);
                    }
                });

        String negativeText = getString(android.R.string.cancel);
        builder.setNegativeButton(negativeText,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // negative button logic
                    }
                });

        AlertDialog dialog = builder.create();
        // display dialog
        dialog.show();
    }
}
