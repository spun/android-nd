package com.spundev.capstone.model.translation;

import com.google.gson.annotations.SerializedName;

public class Translations {
    @SerializedName("translatedText")
    private
    String translatedText;
    @SerializedName("detectedSourceLanguage")
    private
    String detectedSourceLanguage;

    public String getTranslatedText() {
        return translatedText;
    }

    public void setTranslatedText(String translatedText) {
        this.translatedText = translatedText;
    }

    public String getDetectedSourceLanguage() {
        return detectedSourceLanguage;
    }

    public void setDetectedSourceLanguage(String detectedSourceLanguage) {
        this.detectedSourceLanguage = detectedSourceLanguage;
    }
}
