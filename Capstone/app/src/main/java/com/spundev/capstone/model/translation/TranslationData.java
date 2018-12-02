package com.spundev.capstone.model.translation;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TranslationData {
    @SerializedName("translations")
    private
    List<Translations> translations;

    public List<Translations> getTranslations() {
        return translations;
    }

    public void setTranslations(List<Translations> translations) {
        this.translations = translations;
    }
}
