package com.example.dalats.model;

import com.google.gson.annotations.SerializedName;

public class AirQualityResponse {
    @SerializedName("aqi")
    private int aqi;

    @SerializedName("level")
    private String level;

    @SerializedName("mainPollutant")
    private String mainPollutant;

    // Getters
    public int getAqi() { return aqi; }
    public String getLevel() { return level; }
    public String getMainPollutant() { return mainPollutant; }
}