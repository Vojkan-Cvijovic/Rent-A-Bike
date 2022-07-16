package org.bikerent.api.service;

import org.bikerent.api.model.Bike;
import org.bikerent.api.model.BikeActive;
import org.bikerent.api.model.BikeUsed;
import org.bikerent.api.model.Message;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;


public interface BikeService {

    @GET("bikes/list/locations")
    Call<List<String>> listBikeLocations(@Header("Authorization") String authorization);

    @GET("bikes/list/available/location/{selectedLocation}")
    Call<List<Bike>> listAvailableBikes(@Header("Authorization") String authorization, @Path("selectedLocation") String location);

    @GET("bikes/list/all/location/{selectedLocation}")
    Call<List<Bike>> listAllBikes(@Header("Authorization") String authorization, @Path("selectedLocation") String location);

    @PATCH("bikes/update/used")
    Call<BikeUsed> updateBike(@Header("Authorization") String authorization, @Body BikeUsed bike);

    @PATCH("bikes/update/active")
    Call<BikeActive> disableBike(@Header("Authorization") String authorization, @Body BikeActive bike);

    @DELETE("bikes/delete/{id}")
    Call<Message> deleteBike(@Header("Authorization") String authorization, @Path("id") String id);

    @POST("bikes/create")
    Call<Message> creteBike(@Header("Authorization") String authorization, @Body Bike bike);

}
