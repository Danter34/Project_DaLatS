package com.example.dalats.model;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class WeatherResponse {
    @SerializedName("weather") public List<Weather> weather;
    @SerializedName("main") public Main main;
    @SerializedName("wind") public Wind wind;
    @SerializedName("name") public String cityName;

    public static class Weather {
        @SerializedName("main") public String main;
        @SerializedName("description") public String description;
        @SerializedName("icon") public String icon;
    }
    public static class Main {
        @SerializedName("temp") public float temp;
        @SerializedName("humidity") public int humidity;
        @SerializedName("pressure") public int pressure;
        @SerializedName("feels_like") public float feelsLike; // Cảm giác như
    }
    public static class Wind {
        @SerializedName("speed") public float speed;
    }
}