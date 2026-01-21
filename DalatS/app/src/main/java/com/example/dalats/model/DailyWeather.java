package com.example.dalats.model;

public class DailyWeather {
    public String dayName;   // VD: Thứ năm
    public String dateRaw;   // VD: 2023-10-25
    public float minTemp;
    public float maxTemp;
    public String icon;      // icon đại diện trong ngày
    public int pop;          // Tỉ lệ mưa (Probability of precipitation)

    public DailyWeather(String dayName, String dateRaw, float minTemp, float maxTemp, String icon, int pop) {
        this.dayName = dayName;
        this.dateRaw = dateRaw;
        this.minTemp = minTemp;
        this.maxTemp = maxTemp;
        this.icon = icon;
        this.pop = pop;
    }
}