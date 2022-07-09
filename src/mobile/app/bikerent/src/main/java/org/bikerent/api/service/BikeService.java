package org.bikerent.api.service;

import org.bikerent.api.model.Bike;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;


public interface BikeService {

    @GET("bikes/list/locations")
    Call<List<String>> listBikeLocations(@Header("Authorization") String authorization);

    @GET("bikes/list/available")
    Call<List<Bike>> listAvailableBikes();

    @PATCH("bikes/update/used")
    Call<Bike> updateBike(@Body Bike bike);

}
