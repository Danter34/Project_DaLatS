package com.example.dalats.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ForecastResponse {
    @SerializedName("list")
    public List<ForecastItem> list;

    public static class ForecastItem {
        @SerializedName("dt")
        public long dt; // Thời gian dạng số (timestamp)

        // [QUAN TRỌNG] Thêm dòng này để sửa lỗi dt_txt
        @SerializedName("dt_txt")
        public String dt_txt; // Thời gian dạng chữ (VD: "2023-10-25 12:00:00")

        @SerializedName("main")
        public WeatherResponse.Main main;

        @SerializedName("weather")
        public List<WeatherResponse.Weather> weather;

        // (Tùy chọn) Thêm pop nếu muốn lấy tỉ lệ mưa chính xác từ API
        @SerializedName("pop")
        public float pop;
    }
}