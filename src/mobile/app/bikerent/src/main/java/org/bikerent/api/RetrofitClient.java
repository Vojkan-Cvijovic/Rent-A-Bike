package org.bikerent.api;

import org.bikerent.api.service.BikeService;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static RetrofitClient instance = null;
    private final BikeService service;

    private RetrofitClient() {
        Retrofit retrofit = new Retrofit.Builder().baseUrl("https://unsxqopex3.execute-api.eu-central-1.amazonaws.com/stage/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        service = retrofit.create(BikeService.class);
    }

    public static synchronized RetrofitClient getInstance() {
        if (instance == null) {
            instance = new RetrofitClient();
        }
        return instance;
    }

    public BikeService getService() {
        return service;
    }
}
