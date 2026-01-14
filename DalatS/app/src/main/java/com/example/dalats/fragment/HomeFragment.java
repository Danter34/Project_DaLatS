package com.example.dalats.fragment;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dalats.R;
import com.example.dalats.adapter.IncidentAdapter;
import com.example.dalats.api.ApiClient;
import com.example.dalats.model.AirQualityResponse;
import com.example.dalats.model.Incident;
import com.example.dalats.model.WeatherResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.example.dalats.activity.MainActivity;
import com.example.dalats.fragment.MapFragment;
public class HomeFragment extends Fragment {

    // --- Khai báo View ---

    // 1. Thẻ Thời tiết
    private TextView tvTemp, tvWeatherDesc, tvHumidity, tvWind;
    private ImageView imgWeatherIcon;
    private RelativeLayout layoutWeather;

    // 2. Thẻ AQI (Mới)
    private TextView tvAqiScore, tvAqiLevel, tvPollutant;
    private RelativeLayout layoutAir;

    // 3. Phần chung
    private TextView tvUsername;
    private RecyclerView rcvFeed;
    private IncidentAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Ánh xạ View Thời tiết
        tvTemp = view.findViewById(R.id.tv_temp);
        tvWeatherDesc = view.findViewById(R.id.tv_weather_desc);
        tvHumidity = view.findViewById(R.id.tv_humidity);
        tvWind = view.findViewById(R.id.tv_wind);
        imgWeatherIcon = view.findViewById(R.id.img_weather_icon);
        layoutWeather = view.findViewById(R.id.layout_weather);

        // Ánh xạ View AQI
        tvAqiScore = view.findViewById(R.id.tv_aqi_score);
        tvAqiLevel = view.findViewById(R.id.tv_aqi_level);
        tvPollutant = view.findViewById(R.id.tv_pollutant);
        layoutAir = view.findViewById(R.id.layout_air);

        // Ánh xạ phần chung
        tvUsername = view.findViewById(R.id.tv_username);
        rcvFeed = view.findViewById(R.id.rcv_feed);

        // Setup cơ bản
        rcvFeed.setLayoutManager(new LinearLayoutManager(getContext()));
        tvUsername.setText("Khách");

        // GỌI API SONG SONG
        loadWeather();
        loadAirQuality();
        loadFeed();

        // 1. Nút Phản ánh (Report) -> Giả sử mở Toast hoặc Activity mới
        view.findViewById(R.id.btn_feature_report).setOnClickListener(v -> {
            Toast.makeText(getContext(), "Mở chức năng Phản ánh", Toast.LENGTH_SHORT).show();
            // Nếu muốn mở Activity riêng:
            // startActivity(new Intent(getActivity(), ReportActivity.class));
        });

        // 2. Nút Bản đồ (Map) -> Chuyển sang Tab Map (Index 2)
        view.findViewById(R.id.btn_feature_map).setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).switchToTab(2);
            }
        });

        // 3. Nút Cảnh báo (Warning) -> Toast
        view.findViewById(R.id.btn_feature_warning).setOnClickListener(v -> {
            Toast.makeText(getContext(), "Xem danh sách Cảnh báo", Toast.LENGTH_SHORT).show();
        });

        // 4. Nút Hỏi đáp (Chat) -> Chuyển sang Tab Chat (Index 3)
        view.findViewById(R.id.btn_feature_chat).setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).switchToTab(3);
            } else {
                Toast.makeText(getContext(), "Chức năng Hỏi đáp", Toast.LENGTH_SHORT).show();
            }
        });

        // 5. Nút Cá nhân (Profile) -> Chuyển sang Tab Profile (Index 4)
        view.findViewById(R.id.btn_feature_profile).setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).switchToTab(4);
            } else {
                Toast.makeText(getContext(), "Trang cá nhân", Toast.LENGTH_SHORT).show();
            }
        });

        // 6. Nút Thống kê (Stats) -> Toast
        view.findViewById(R.id.btn_feature_stats).setOnClickListener(v -> {
            Toast.makeText(getContext(), "Xem thống kê số liệu", Toast.LENGTH_SHORT).show();
        });
        return view;
    }

    // --- LOGIC 1: THỜI TIẾT ---
    private void loadWeather() {
        ApiClient.getEnviService().getWeather().enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    WeatherResponse w = response.body();
                    updateWeatherUI(w.getTemperature(), w.getDescription(), w.getHumidity(), w.getWindSpeed());
                }
            }
            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                tvWeatherDesc.setText("Lỗi kết nối");
            }
        });
    }

    private void updateWeatherUI(double temp, String description, int humidity, double windSpeed) {
        if (getContext() == null) return;

        tvTemp.setText(Math.round(temp) + "°");

        String capDesc = (description != null && !description.isEmpty())
                ? description.substring(0, 1).toUpperCase() + description.substring(1)
                : "";
        tvWeatherDesc.setText(capDesc);

        // Thêm icon emoji vào text để hiển thị trong Chip
        tvHumidity.setText("💧 " + humidity + "%");
        tvWind.setText("💨 " + String.format("%.1f", windSpeed) + " m/s");

        // Logic màu sắc (Giữ nguyên như cũ vì đã tốt rồi)
        String condition = (description != null) ? description.toLowerCase() : "";
        int startColor, endColor, iconResId;

        if (condition.contains("mưa") || condition.contains("dông")) {
            startColor = Color.parseColor("#373B44"); endColor = Color.parseColor("#4286f4");
            iconResId = R.drawable.rainy;
        }
        else if (condition.contains("nắng") && temp > 25) {
            startColor = Color.parseColor("#FF512F"); endColor = Color.parseColor("#DD2476");
            iconResId = R.drawable.sunny;
        }
        else if (temp < 18) {
            startColor = Color.parseColor("#00c6ff"); endColor = Color.parseColor("#0072ff");
            iconResId = R.drawable.cloudy;
        }
        else {
            startColor = Color.parseColor("#8E2DE2"); endColor = Color.parseColor("#4A00E0");
            iconResId = R.drawable.tt;
        }

        applyGradient(layoutWeather, startColor, endColor);
        imgWeatherIcon.setImageResource(iconResId);
    }

    // --- LOGIC 2: KHÔNG KHÍ (AQI) ---
    private void loadAirQuality() {
        ApiClient.getEnviService().getAirQuality().enqueue(new Callback<AirQualityResponse>() {
            @Override
            public void onResponse(Call<AirQualityResponse> call, Response<AirQualityResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AirQualityResponse air = response.body();
                    updateAirQualityUI(air.getAqi(), air.getLevel(), air.getMainPollutant());
                }
            }
            @Override
            public void onFailure(Call<AirQualityResponse> call, Throwable t) {
                tvAqiLevel.setText("Lỗi tải");
            }
        });
    }

    private void updateAirQualityUI(int aqi, String level, String pollutant) {
        if (getContext() == null) return;

        tvAqiScore.setText(String.valueOf(aqi));
        tvAqiLevel.setText(level);
        tvPollutant.setText(pollutant != null ? pollutant.toUpperCase() : "PM2.5");

        // Logic màu AQI (Giữ nguyên)
        int startColor, endColor;
        if (aqi <= 50) {
            startColor = Color.parseColor("#11998e"); endColor = Color.parseColor("#38ef7d");
        } else if (aqi <= 100) {
            startColor = Color.parseColor("#f12711"); endColor = Color.parseColor("#f5af19");
        } else if (aqi <= 150) {
            startColor = Color.parseColor("#FF512F"); endColor = Color.parseColor("#DD2476");
        } else {
            startColor = Color.parseColor("#8E2DE2"); endColor = Color.parseColor("#4A00E0");
        }
        applyGradient(layoutAir, startColor, endColor);
    }

    // --- TIỆN ÍCH CHUNG ---
    private void applyGradient(View view, int startColor, int endColor) {
        GradientDrawable gradient = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{startColor, endColor});
        gradient.setCornerRadius(dpToPx(16)); // Bo góc 16dp
        view.setBackground(gradient);
    }

    private int dpToPx(int dp) {
        if (getContext() == null) return 0;
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    // --- LOAD FEED (Giữ nguyên) ---
    private void loadFeed() {
        ApiClient.getIncidentService().getPublicFeed().enqueue(new Callback<List<Incident>>() {
            @Override
            public void onResponse(Call<List<Incident>> call, Response<List<Incident>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter = new IncidentAdapter(getContext(), response.body());
                    rcvFeed.setAdapter(adapter);
                }
            }
            @Override
            public void onFailure(Call<List<Incident>> call, Throwable t) {}
        });
    }
}