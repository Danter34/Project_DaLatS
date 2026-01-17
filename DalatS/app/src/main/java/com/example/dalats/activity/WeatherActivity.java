package com.example.dalats.activity;

import android.os.Bundle;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.dalats.R;
import com.example.dalats.adapter.ForecastAdapter;
import com.example.dalats.api.WeatherClient;
import com.example.dalats.model.ForecastResponse;
import com.example.dalats.model.WeatherResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WeatherActivity extends AppCompatActivity {

    // --- ĐIỀN KEY CỦA BẠN VÀO ĐÂY ---
    private static final String API_KEY = "YOUR_OPENWEATHERMAP_API_KEY";
    private static final String CITY = "Dalat,VN";

    private TextView tvMainTemp, tvDesc, tvMinMax, tvHum, tvWind, tvFeelsLike;
    private RecyclerView rcvForecast;
    private RelativeLayout rootLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        initViews();
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        loadCurrentWeather();
        loadForecast();
    }

    private void initViews() {
        rootLayout = findViewById(R.id.root_weather_layout);
        tvMainTemp = findViewById(R.id.tv_main_temp);
        tvDesc = findViewById(R.id.tv_main_desc);
        tvMinMax = findViewById(R.id.tv_min_max);
        tvHum = findViewById(R.id.tv_humidity);
        tvWind = findViewById(R.id.tv_wind);
        tvFeelsLike = findViewById(R.id.tv_feels_like);
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        rcvForecast = findViewById(R.id.rcv_forecast);
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        rcvForecast.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
    }
    private void loadCurrentWeather() {
        WeatherClient.getService().getCurrentWeather(CITY, API_KEY, "metric", "vi")
                .enqueue(new Callback<WeatherResponse>() {
                    @Override
                    public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            updateUI(response.body());
                        }
                    }
                    @Override
                    public void onFailure(Call<WeatherResponse> call, Throwable t) {
                        Toast.makeText(WeatherActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateUI(WeatherResponse w) {
        tvMainTemp.setText(Math.round(w.main.temp) + "°");

        String desc = w.weather.get(0).description;
        tvDesc.setText(desc.substring(0, 1).toUpperCase() + desc.substring(1));

        tvMinMax.setText("H: " + Math.round(w.main.temp + 2) + "°  L: " + Math.round(w.main.temp - 2) + "°");
        tvHum.setText(w.main.humidity + "%");
        tvWind.setText(w.wind.speed + " m/s");
        tvFeelsLike.setText(Math.round(w.main.feelsLike) + "°");

        // Đổi màu nền
        String condition = w.weather.get(0).main.toLowerCase();
        int bgResId = R.drawable.bg_gradient_sunny; // Mặc định

        if (condition.contains("rain") || condition.contains("drizzle") || condition.contains("thunder")) {
            bgResId = R.drawable.bg_gradient_rainy;
        } else if (condition.contains("cloud") || condition.contains("mist") || condition.contains("fog")) {
            bgResId = R.drawable.bg_gradient_cloudy;
        }

        rootLayout.setBackgroundResource(bgResId);
    }

    private void loadForecast() {
        WeatherClient.getService().getForecast(CITY, API_KEY, "metric", "vi")
                .enqueue(new Callback<ForecastResponse>() {
                    @Override
                    public void onResponse(Call<ForecastResponse> call, Response<ForecastResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            ForecastAdapter adapter = new ForecastAdapter(response.body().list);
                            rcvForecast.setAdapter(adapter);
                        }
                    }
                    @Override
                    public void onFailure(Call<ForecastResponse> call, Throwable t) {}
                });
    }
}