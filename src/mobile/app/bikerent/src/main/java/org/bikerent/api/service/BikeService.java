package org.bikerent.api.service;

import org.bikerent.api.model.Bike;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.Path;


public interface BikeService {

    @GET("bikes/list/locations")
    Call<List<String>> listBikeLocations(@Header("Authorization") String authorization);

    @GET("bikes/list/available/location/{selectedLocation}")
    Call<List<Bike>> listAvailableBikes(@Header("Authorization") String authorization, @Path("selectedLocation") String location);

    @PATCH("bikes/update/used")
    Call<Bike> updateBike(@Header("Authorization") String authorization, @Body Bike bike);

}
