package com.spundev.capstone.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.annotation.NonNull;
import android.util.Log;

import com.spundev.capstone.util.PreferencesUtils;

import java.util.HashMap;
import java.util.Locale;

public class TTSViewModel extends AndroidViewModel implements TextToSpeech.OnInitListener {

    private static final String TAG = "TTSViewModel";

    private TextToSpeech tts;
    private final MutableLiveData<Boolean> isPlaying;
    private final MutableLiveData<String> ttsText;

    private final String TTS_UTTERANCE_ID = "TTS_UTTERANCE_ID";

    public TTSViewModel(@NonNull Application application) {
        super(application);

        // tts setup
        // We create and use the tts object from the view model to prevent
        // interruptions of the playback on rotation, etc
        setupTextToSpeech();
        isPlaying = new MutableLiveData<>();
        ttsText = new MutableLiveData<>();
    }


    private void setupTextToSpeech() {
        // New tts object
        tts = new TextToSpeech(getApplication(), this);
        // Set default language
        tts.setLanguage(Locale.US);
    }


    // Play audio
    public void startAudio() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Play current string
            tts.speak(ttsText.getValue(), TextToSpeech.QUEUE_FLUSH, null, TTS_UTTERANCE_ID);
        } else {
            HashMap<String, String> params = new HashMap<>();
            params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, TTS_UTTERANCE_ID);
            tts.speak(ttsText.getValue(), TextToSpeech.QUEUE_FLUSH, params);
        }
    }

    public void stopAudio() {
        tts.stop();
        isPlaying.postValue(false);
    }

    public LiveData<Boolean> getIsPlaying() {
        return isPlaying;
    }

    public LiveData<String> getTTSText() {
        return ttsText;
    }

    public void setTTSText(String value) {
        ttsText.postValue(value);
    }

    @Override
    public void onInit(int status) {
        // TTS initialization
        if (status == TextToSpeech.SUCCESS) {
            // If tts initialize, set the Utterance listener (tts events)
            tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override
                public void onStart(String s) {
                    isPlaying.postValue(true);
                }

                @Override
                public void onDone(String s) {
                    isPlaying.postValue(false);
                }

                @Override
                public void onError(String s) {
                    isPlaying.postValue(false);
                }
            });

            // Set locale
            int result;
            switch (PreferencesUtils.getPreferenceLanguage(getApplication())) {
                case "es":
                    result = tts.setLanguage(new Locale("spa", "ESP"));
                    break;
                case "en":
                    result = tts.setLanguage(new Locale("en", "US"));
                    break;
                case "it":
                    result = tts.setLanguage(new Locale("it", "IT"));
                    break;
                case "fr":
                    result = tts.setLanguage(new Locale("fr", "FR"));
                    break;
                case "de":
                    result = tts.setLanguage(new Locale("de", "GER"));
                    break;
                case "pt":
                    result = tts.setLanguage(new Locale("pt", "PT"));
                    break;
                default:
                    result = tts.setLanguage(new Locale("en", "US"));
                    break;
            }

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(TAG, "This Language is not supported");
            }
        } else {
            Log.e(TAG, "Initialization Failed!");
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        tts.shutdown();
    }
}
