package com.example.dalats.activity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.dalats.R;
import com.example.dalats.adapter.DailyAdapter;
import com.example.dalats.adapter.ForecastAdapter;
import com.example.dalats.api.WeatherClient;
import com.example.dalats.model.DailyWeather;
import com.example.dalats.model.ForecastResponse;
import com.example.dalats.model.WeatherResponse;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WeatherActivity extends AppCompatActivity {

    private static final String API_KEY = "api-key";
    private static final String CITY_NAME = "Dalat";
    private static final String LANG = "vi";
    private static final String UNITS = "metric";

    private TextView tvCityName, tvMainTemp, tvDesc, tvMinMax;
    private ImageView imgIllustration, btnMenu;
    private RecyclerView rcvForecast, rcvDailyForecast; // Thêm rcv thứ 2

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        initViews();
        btnMenu.setOnClickListener(v -> finish());

        loadCurrentWeather();
        loadForecast();
    }

    private void initViews() {
        tvCityName = findViewById(R.id.tv_city_name);
        tvMainTemp = findViewById(R.id.tv_main_temp);
        tvDesc = findViewById(R.id.tv_main_desc);
        tvMinMax = findViewById(R.id.tv_min_max);
        imgIllustration = findViewById(R.id.img_illustration);
        btnMenu = findViewById(R.id.btn_menu);

        // 1. RecyclerView Ngang (Theo giờ)
        rcvForecast = findViewById(R.id.rcv_forecast);
        rcvForecast.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        // 2. RecyclerView Dọc (Theo ngày) - MỚI
        rcvDailyForecast = findViewById(R.id.rcv_daily_forecast);
        rcvDailyForecast.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
    }

    private void loadCurrentWeather() {
        WeatherClient.getService().getCurrentWeather(CITY_NAME, API_KEY, UNITS, LANG)
                .enqueue(new Callback<WeatherResponse>() {
                    @Override
                    public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            updateCurrentUI(response.body());
                        }
                    }
                    @Override
                    public void onFailure(Call<WeatherResponse> call, Throwable t) {
                        Toast.makeText(WeatherActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadForecast() {
        WeatherClient.getService().getForecast(CITY_NAME, API_KEY, UNITS, LANG)
                .enqueue(new Callback<ForecastResponse>() {
                    @Override
                    public void onResponse(Call<ForecastResponse> call, Response<ForecastResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<ForecastResponse.ForecastItem> rawList = response.body().list;

                            // 1. Hiển thị list ngang (24h tới) - Giới hạn 8 item đầu
                            ForecastAdapter hourlyAdapter = new ForecastAdapter(rawList.subList(0, Math.min(rawList.size(), 8)));
                            rcvForecast.setAdapter(hourlyAdapter);

                            // 2. Xử lý dữ liệu để hiển thị list dọc (5 ngày tới)
                            List<DailyWeather> dailyList = processDailyForecast(rawList);
                            DailyAdapter dailyAdapter = new DailyAdapter(dailyList);
                            rcvDailyForecast.setAdapter(dailyAdapter);
                        }
                    }
                    @Override
                    public void onFailure(Call<ForecastResponse> call, Throwable t) {}
                });
    }

    // --- LOGIC GOM NHÓM NGÀY ---
    // Trong file WeatherActivity.java

    private List<DailyWeather> processDailyForecast(List<ForecastResponse.ForecastItem> rawList) {
        Map<String, DailyWeather> map = new HashMap<>();
        List<String> uniqueDates = new ArrayList<>();
        SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat displayFormat = new SimpleDateFormat("EEEE", new Locale("vi", "VN"));

        for (ForecastResponse.ForecastItem item : rawList) {
            Date date = new Date(item.dt * 1000);
            String dateKey = dayFormat.format(date);

            // Tính % mưa của khung giờ hiện tại (0.2 -> 20)
            int currentPop = (int) (item.pop * 100);

            if (!map.containsKey(dateKey)) {
                String dayName = displayFormat.format(date);
                dayName = dayName.substring(0, 1).toUpperCase() + dayName.substring(1);

                if (dateKey.equals(dayFormat.format(new Date()))) dayName = "Hôm nay";

                // Khởi tạo ngày mới với pop hiện tại
                map.put(dateKey, new DailyWeather(dayName, dateKey, 100, -100, item.weather.get(0).main, currentPop));
                uniqueDates.add(dateKey);
            }

            DailyWeather daily = map.get(dateKey);

            // Cập nhật nhiệt độ Min/Max
            if (item.main.temp < daily.minTemp) daily.minTemp = item.main.temp;
            if (item.main.temp > daily.maxTemp) daily.maxTemp = item.main.temp;

            // [QUAN TRỌNG] Cập nhật % mưa: Lấy số lớn nhất trong ngày
            // Ví dụ: Sáng mưa 10%, Chiều mưa 80% -> Hiển thị 80% cho cả ngày
            if (currentPop > daily.pop) {
                daily.pop = currentPop;
            }

            // Logic ưu tiên icon mưa: Nếu trong ngày có lúc nào mưa, set icon ngày đó là mưa luôn
            if (item.weather.get(0).main.toLowerCase().contains("rain")) {
                daily.icon = "Rain";
            } else if (daily.icon.equals("Rain")) {
                // Giữ nguyên là Rain nếu đã set trước đó
            } else if (item.dt_txt.contains("12:00")) {
                // Nếu không mưa, ưu tiên lấy icon lúc giữa trưa
                daily.icon = item.weather.get(0).main;
            }
        }

        List<DailyWeather> result = new ArrayList<>();
        for (String key : uniqueDates) {
            result.add(map.get(key));
        }
        return result;
    }

    private void updateCurrentUI(WeatherResponse w) {
        tvCityName.setText("Ward 8"); // Giống hình
        tvMainTemp.setText(Math.round(w.main.temp) + "°");

        String desc = w.weather.get(0).description;
        tvDesc.setText(desc.substring(0, 1).toUpperCase() + desc.substring(1));

        int max = Math.round(w.main.temp) + 3;
        int min = Math.round(w.main.temp) - 2;
        tvMinMax.setText(max + "° / " + min + "° Cảm giác như " + Math.round(w.main.feelsLike) + "°");

        // Đổi ảnh minh họa
        if (w.weather.get(0).main.toLowerCase().contains("rain")) {
            imgIllustration.setImageResource(R.drawable.rai);
        } else {
            imgIllustration.setImageResource(R.drawable.coffe);
        }
    }
}