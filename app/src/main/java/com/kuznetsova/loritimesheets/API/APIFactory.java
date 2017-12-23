package com.kuznetsova.loritimesheets.API;

import android.support.annotation.NonNull;

import com.kuznetsova.loritimesheets.API.APIService;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class APIFactory {
   // private static final String BASE_URL = "http://192.168.1.86:8080";//домашний
    private static final String BASE_URL = "http://192.168.43.229:8080";

    private static final OkHttpClient CLIENT = new OkHttpClient();


    @NonNull
    public static APIService getAPIService() {
        return getRetrofit().create(APIService.class);
    }

    @NonNull
    public static Retrofit getRetrofit() {
        return new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .client(CLIENT)
                .baseUrl(BASE_URL)
                .build();
    }
}
