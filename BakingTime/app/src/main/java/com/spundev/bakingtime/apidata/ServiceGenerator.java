package com.spundev.bakingtime.apidata;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by spundev.
 */

class ServiceGenerator {
    private static final String BASE_URL = "http://go.udacity.com";

    private static final Retrofit.Builder builder =
            new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create());

    private static final Retrofit retrofit = builder.build();

    private static OkHttpClient.Builder httpClient =
            new OkHttpClient.Builder();

    static <S> S createService(
            Class<S> serviceClass) {
        return retrofit.create(serviceClass);
    }
}
