package com.example.dalats.api;

import com.example.dalats.model.Incident;
import com.example.dalats.model.IncidentMap;
import com.example.dalats.model.WeatherResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface IncidentService {
    @GET("api/Incidents/map")
    Call<List<IncidentMap>> getMapData();
    @GET("api/Incidents/feed")
    Call<List<Incident>> getPublicFeed();
}