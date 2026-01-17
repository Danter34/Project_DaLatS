package com.example.dalats.api;

import com.example.dalats.model.AirQualityResponse;
import com.example.dalats.model.wt;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface EnviService {

    //ẩn để giảm số lần rq khi app đang ở prototype

    @GET("api/e/weather")
    Call<wt> getWeather();
    @GET("api/E/air-quality")
    Call<AirQualityResponse> getAirQuality();
}