package com.example.dalats.api;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.PUT;

public interface ApiService {
    @Headers("Content-Type: application/json")
    @PUT("api/Auth/update-fcm")
    Call<Void> updateFcmToken(@Body String token);
}
