package com.spundev.capstone.apidata;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

class ServiceGenerator {
    private static final String BASE_URL = "https://translation.googleapis.com";

    private static final Retrofit.Builder builder =
            new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create());

    private static final Retrofit retrofit = builder.build();

    private static OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

    static <S> S createService(Class<S> serviceClass) {
        return retrofit.create(serviceClass);
    }
}