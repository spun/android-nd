package com.spundev.capstone.model.translation;

import com.google.gson.annotations.SerializedName;

public class TranslationResponse {
    @SerializedName("data")
    private
    TranslationData data;

    public TranslationData getData() {
        return data;
    }

    public void setData(TranslationData data) {
        this.data = data;
    }

}
