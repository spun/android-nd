package com.spundev.capstone.apidata;

public class ApiUtils {
    // Create a new service for the TranslateApi
    public static TranslateService getTranslateService() {
        return ServiceGenerator.createService(TranslateService.class);
    }
}
