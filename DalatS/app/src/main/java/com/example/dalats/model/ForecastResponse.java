package com.example.dalats.model;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ForecastResponse {
    @SerializedName("list")
    public List<ForecastItem> list;

    public static class ForecastItem {
        @SerializedName("dt")
        public long dt; // Thời gian

        @SerializedName("main")
        public com.example.dalats.model.WeatherResponse.Main main;

        @SerializedName("weather")
        public List<com.example.dalats.model.WeatherResponse.Weather> weather;

        @SerializedName("dt_txt")
        public String dtTxt;
    }
}