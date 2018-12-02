package com.spundev.capstone.ui;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.spundev.capstone.R;
import com.spundev.capstone.util.PreferencesUtils;

import java.util.ArrayList;
import java.util.Locale;

public class STTActivity extends AppCompatActivity {

    private static final String TAG = "STTActivity";


    private static final int REQ_CODE_SPEECH_INPUT = 100;
    private TextView mVoiceInputTextView;

    private static final String STATE_TEXT_VIEW = "STATE_TEXT_VIEW";

    public static void start(Context context) {
        Intent starter = new Intent(context, STTActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_TEXT_VIEW,  mVoiceInputTextView.getText().toString());
    }


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        String textSaved = savedInstanceState.getString(STATE_TEXT_VIEW);
        mVoiceInputTextView.setText(textSaved);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stt);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Toolbar back arrow
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }

        mVoiceInputTextView = findViewById(R.id.stringOutputSTT);
        LinearLayout btnSpeakContainer = findViewById(R.id.btnSpeakContainer);
        btnSpeakContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startVoiceInput();
            }
        });
    }

    private void startVoiceInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        Locale sstLocale;
        String preferenceLanguage = PreferencesUtils.getPreferenceLanguage(this);

        switch (preferenceLanguage) {
            case "es":
                sstLocale = new Locale("es", "ES");
                break;
            case "en":
                sstLocale = new Locale("en", "US");
                break;
            case "it":
                sstLocale = new Locale("it", "IT");
                break;
            case "fr":
                sstLocale = new Locale("fr", "FR");
                break;
            case "de":
                sstLocale = new Locale("de", "GER");
                break;
            case "pt":
                sstLocale = new Locale("pt", "PT");
                break;
            default:
                sstLocale = Locale.getDefault();
                break;
        }


        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, sstLocale);
        //intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.stt_popup_message));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "startVoiceInput: ", e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                    if (Locale.getDefault().getLanguage().equals(PreferencesUtils.getPreferenceLanguage(this))) {
                        mVoiceInputTextView.setText(result.get(0));
                    } else {
                        mVoiceInputTextView.setText(result.get(0));
                        // Paid API, removed use
                        //Translate(result.get(0), PreferencesUtils.getPreferenceLanguage(this), Locale.getDefault().getLanguage());
                    }
                }
                break;
            }
        }
    }

    private void Translate(final String text, final String langFrom, final String langTo) {

        // sample code
        /*TranslateService translateService = ApiUtils.getTranslateService();
        translateService.translate(TranslationUtils.getApiKey(), text, langTo).enqueue(new Callback<TranslationResponse>() {
            @Override
            public void onResponse(@NonNull Call<TranslationResponse> call, @NonNull Response<TranslationResponse> response) {
                if (response.isSuccessful()) {

                    translatedText = response.body().getData().getTranslations().get(0).getTranslatedText();
                    SpannableString span1 = new SpannableString(langFrom + ": " + text + '\n');
                    SpannableString span2 = new SpannableString(Locale.getDefault().getLanguage() + ": " + translatedText);

                    span1.setSpan(new RelativeSizeSpan(0.5f), 0, span1.length(), 0);
                    span1.setSpan(new ForegroundColorSpan(Color.RED), 0, 3, 0);
                    span1.setSpan(new ForegroundColorSpan(Color.LTGRAY), 4, span1.length(), 0);

                    span2.setSpan(new RelativeSizeSpan(1f), 0, span2.length(), 0);
                    span2.setSpan(new ForegroundColorSpan(Color.RED), 0, 3, 0);
                    span2.setSpan(new ForegroundColorSpan(Color.BLACK), 4, span2.length(), 0);


                    mVoiceInputTextView.setText(TextUtils.concat(span1, span2));

                    localeText = text;


                } else {
                    Log.d(TAG, "onResponse: " + response.message());
                    Toast.makeText(STTActivity.this, R.string.translate_service_failed, Toast.LENGTH_SHORT).show();
                    mVoiceInputTextView.setText(text);
                }
            }

            @Override
            public void onFailure(@NonNull Call<TranslationResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "onFailure: ", t);
                Toast.makeText(STTActivity.this, R.string.translate_service_failed, Toast.LENGTH_SHORT).show();
                mVoiceInputTextView.setText(text);
            }
        });*/
    }
}
