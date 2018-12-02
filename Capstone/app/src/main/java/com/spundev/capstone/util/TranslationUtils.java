package com.spundev.capstone.util;

import com.spundev.capstone.BuildConfig;

public class TranslationUtils {

    private static final String API_KEY = BuildConfig.TRANSLATE_API_KEY;

    public static String getApiKey() {
        return API_KEY;
    }

    /*
    public static void restExample() {

        // sample code
        TranslateService translateService = ApiUtils.getTranslateService();
        translateService.translate("AIzaSyBwzkzTs1WevN5EJb9cgINgRJI4u1F9_EY", "Hello", "es").enqueue(new Callback<TranslationResponse>() {
            @Override
            public void onResponse(Call<TranslationResponse> call, Response<TranslationResponse> response) {
                if (response.isSuccessful()) {
                    String finalText = response.body().getData().getTranslations().get(0).getTranslatedText();
                    Log.d(TAG, "onResponse: " + finalText);
                } else {
                    // no access?
                }
            }

            @Override
            public void onFailure(Call<TranslationResponse> call, Throwable t) {
                // failure
            }
        });
    }*/
}
