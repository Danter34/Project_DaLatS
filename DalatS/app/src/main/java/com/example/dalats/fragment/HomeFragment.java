package com.example.dalats.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout; // Cần import cái này
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
import com.example.dalats.activity.DashboardActivity;
import com.example.dalats.activity.NotificationActivity;
import com.example.dalats.activity.ReportIncidentActivity;
import com.example.dalats.activity.SearchIncidentActivity;
import com.example.dalats.activity.WeatherActivity;
import com.example.dalats.adapter.IncidentAdapter;
import com.example.dalats.api.ApiClient;
import com.example.dalats.model.AirQualityResponse;
import com.example.dalats.model.Incident;
import com.example.dalats.model.NotificationDTO;

import com.example.dalats.model.wt;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.example.dalats.activity.MainActivity;

public class HomeFragment extends Fragment {

    // --- Khai báo View ---
    private TextView tvTemp, tvWeatherDesc, tvHumidity, tvWind;
    private ImageView imgWeatherIcon;
    private RelativeLayout layoutWeather;
    private TextView tvAqiScore, tvAqiLevel, tvPollutant;
    private RelativeLayout layoutAir;
    private TextView tvUsername;
    private RecyclerView rcvFeed;
    private IncidentAdapter adapter;

    // --- KHAI BÁO BIẾN MỚI CHO THÔNG BÁO ---
    private FrameLayout btnNotificationContainer; // Khung chứa chuông
    private View viewUnreadBadge; // Chấm đỏ

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Gọi hàm ánh xạ View
        initViews(view);

        // Setup RecyclerView
        rcvFeed.setLayoutManager(new LinearLayoutManager(getContext()));
        tvUsername.setText("Khách");

        // Gọi API dữ liệu
        loadWeather();
        loadAirQuality();
        loadFeed();

        // --- SỰ KIỆN CLICK (Đã sửa lại dùng Container) ---

        // 1. Click vào Chuông -> Mở trang thông báo
        if (btnNotificationContainer != null) {
            btnNotificationContainer.setOnClickListener(v -> {
                startActivity(new Intent(getActivity(), NotificationActivity.class));
            });
        }

        // 2. Click thẻ Thời tiết
        if (layoutWeather != null) {
            layoutWeather.setOnClickListener(v -> {
                startActivity(new Intent(getActivity(), WeatherActivity.class));
            });
        }

        // --- CÁC NÚT TIỆN ÍCH ---
        // Sử dụng findViewById trực tiếp từ view cha cho gọn
        view.findViewById(R.id.btn_feature_report).setOnClickListener(v -> startActivity(new Intent(getActivity(), ReportIncidentActivity.class)));

        view.findViewById(R.id.btn_feature_warning).setOnClickListener(v -> startActivity(new Intent(getActivity(), SearchIncidentActivity.class)));

        view.findViewById(R.id.btn_feature_stats).setOnClickListener(v -> startActivity(new Intent(getActivity(), DashboardActivity.class)));

        view.findViewById(R.id.btn_feature_weather_shortcut).setOnClickListener(v -> startActivity(new Intent(getActivity(), WeatherActivity.class)));

        view.findViewById(R.id.btn_feature_incident_list).setOnClickListener(v -> startActivity(new Intent(getActivity(), SearchIncidentActivity.class)));

        // Các nút chuyển Tab
        view.findViewById(R.id.btn_feature_map).setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) ((MainActivity) getActivity()).switchToTab(2);
        });
        view.findViewById(R.id.btn_feature_chat).setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) ((MainActivity) getActivity()).switchToTab(3);
        });
        view.findViewById(R.id.btn_feature_profile).setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) ((MainActivity) getActivity()).switchToTab(4);
        });

        return view;
    }

    // Hàm ánh xạ View (Giúp code gọn gàng hơn)
    private void initViews(View view) {
        tvTemp = view.findViewById(R.id.tv_temp);
        tvWeatherDesc = view.findViewById(R.id.tv_weather_desc);
        tvHumidity = view.findViewById(R.id.tv_humidity);
        tvWind = view.findViewById(R.id.tv_wind);
        imgWeatherIcon = view.findViewById(R.id.img_weather_icon);
        layoutWeather = view.findViewById(R.id.layout_weather);

        tvAqiScore = view.findViewById(R.id.tv_aqi_score);
        tvAqiLevel = view.findViewById(R.id.tv_aqi_level);
        tvPollutant = view.findViewById(R.id.tv_pollutant);
        layoutAir = view.findViewById(R.id.layout_air);

        tvUsername = view.findViewById(R.id.tv_username);
        rcvFeed = view.findViewById(R.id.rcv_feed);

        // --- ÁNH XẠ ĐÚNG ID TỪ FILE XML MỚI ---
        btnNotificationContainer = view.findViewById(R.id.btn_notification_container);
        viewUnreadBadge = view.findViewById(R.id.view_unread_badge);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUsername();
        checkUnreadNotifications(); // Kiểm tra thông báo mỗi khi quay lại màn hình
    }

    // --- LOGIC KIỂM TRA THÔNG BÁO ---
    private void checkUnreadNotifications() {
        if (getContext() == null) return;

        SharedPreferences pref = getContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        String token = pref.getString("TOKEN", null);

        // Nếu chưa đăng nhập thì ẩn chấm đỏ
        if (token == null) {
            if (viewUnreadBadge != null) viewUnreadBadge.setVisibility(View.GONE);
            return;
        }

        // Gọi API lấy danh sách thông báo
        ApiClient.getNotificationService().getMyNotifications().enqueue(new Callback<List<NotificationDTO>>() {
            @Override
            public void onResponse(Call<List<NotificationDTO>> call, Response<List<NotificationDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    boolean hasUnread = false;
                    for (NotificationDTO item : response.body()) {
                        if (!item.isRead()) { // Nếu có tin chưa đọc
                            hasUnread = true;
                            break;
                        }
                    }
                    // Hiện hoặc ẩn chấm đỏ
                    if (viewUnreadBadge != null) {
                        viewUnreadBadge.setVisibility(hasUnread ? View.VISIBLE : View.GONE);
                    }
                }
            }
            @Override
            public void onFailure(Call<List<NotificationDTO>> call, Throwable t) {}
        });
    }

    private void updateUsername() {
        if (getContext() == null) return;
        SharedPreferences pref = getContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        String fullName = pref.getString("FULL_NAME", null);
        if (fullName != null && !fullName.isEmpty()) {
            tvUsername.setText(fullName);
        } else {
            tvUsername.setText("Khách");
        }
    }

    // --- LOGIC THỜI TIẾT ---
    private void loadWeather() {
        ApiClient.getEnviService().getWeather().enqueue(new Callback<wt>() {
            @Override
            public void onResponse(Call<wt> call, Response<wt> response) {
                if (response.isSuccessful() && response.body() != null) {
                    wt w = response.body();
                    updateWeatherUI(w.getTemperature(), w.getDescription(), w.getHumidity(), w.getWindSpeed());
                }
            }
            @Override
            public void onFailure(Call<wt> call, Throwable t) {
                tvWeatherDesc.setText("Lỗi kết nối");
            }
        });
    }

    private void updateWeatherUI(double temp, String description, int humidity, double windSpeed) {
        if (getContext() == null) return;

        tvTemp.setText(Math.round(temp) + "°");
        String capDesc = (description != null && !description.isEmpty()) ? description.substring(0, 1).toUpperCase() + description.substring(1) : "";
        tvWeatherDesc.setText(capDesc);
        tvHumidity.setText("💧 " + humidity + "%");
        tvWind.setText("💨 " + String.format("%.1f", windSpeed) + " m/s");

        String condition = (description != null) ? description.toLowerCase() : "";
        int startColor, endColor, iconResId;

        if (condition.contains("mưa") || condition.contains("dông")) {
            startColor = Color.parseColor("#373B44"); endColor = Color.parseColor("#4286f4");
            iconResId = R.drawable.rainy;
        } else if (condition.contains("nắng") && temp > 25) {
            startColor = Color.parseColor("#FF512F"); endColor = Color.parseColor("#DD2476");
            iconResId = R.drawable.sunny;
        } else if (temp < 18) {
            startColor = Color.parseColor("#00c6ff"); endColor = Color.parseColor("#0072ff");
            iconResId = R.drawable.cloudy;
        } else {
            startColor = Color.parseColor("#8E2DE2"); endColor = Color.parseColor("#4A00E0");
            iconResId = R.drawable.tt;
        }
        applyGradient(layoutWeather, startColor, endColor);
        imgWeatherIcon.setImageResource(iconResId);
    }

    // --- LOGIC KHÔNG KHÍ ---
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
                if (tvAqiLevel != null) tvAqiLevel.setText("Lỗi");
            }
        });
    }

    private void updateAirQualityUI(int aqi, String level, String pollutant) {
        if (getContext() == null) return;
        tvAqiScore.setText(String.valueOf(aqi));
        tvAqiLevel.setText(level);
        tvPollutant.setText(pollutant != null ? pollutant.toUpperCase() : "PM2.5");

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

    private void applyGradient(View view, int startColor, int endColor) {
        GradientDrawable gradient = new GradientDrawable(GradientDrawable.Orientation.TL_BR, new int[]{startColor, endColor});
        gradient.setCornerRadius(dpToPx(16));
        view.setBackground(gradient);
    }

    private int dpToPx(int dp) {
        if (getContext() == null) return 0;
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    // --- LOAD FEED ---
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