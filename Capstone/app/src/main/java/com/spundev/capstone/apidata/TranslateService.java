package com.spundev.capstone.apidata;

import com.spundev.capstone.model.translation.TranslationResponse;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface TranslateService {
    @POST("/language/translate/v2")
    @FormUrlEncoded
    Call<TranslationResponse> translate(@Field("key") String key,
                                        @Field("q") String q,
                                        @Field("target") String target);
}
