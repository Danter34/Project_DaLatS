package com.example.dalats.activity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.dalats.R;
import com.example.dalats.api.ApiClient;
import com.example.dalats.api.ApiService; // Import Interface API
import com.example.dalats.fragment.ChatFragment;
import com.example.dalats.fragment.HomeFragment;
import com.example.dalats.fragment.MapFragment;
import com.example.dalats.fragment.ProfileFragment;
import com.google.firebase.messaging.FirebaseMessaging;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    // Khai báo View
    private LinearLayout btnHome, btnMap, btnChat, btnProfile, btnReport;
    private ImageView imgHome, imgMap, imgChat, imgProfile;
    private TextView tvHome, tvMap, tvChat, tvProfile;

    // Màu sắc
    private int colorActive;
    private int colorInactive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Cấu hình Token đăng nhập
        SharedPreferences pref = getSharedPreferences("UserSession", MODE_PRIVATE);
        String savedToken = pref.getString("TOKEN", null);
        if (savedToken != null) {
            ApiClient.setAuthToken(savedToken);
        }

        // 2. Khởi tạo màu sắc
        colorActive = Color.parseColor("#4CAF50");
        colorInactive = Color.parseColor("#757575");

        // 3. Ánh xạ View
        initViews();

        // 4. Mặc định load Home
        loadFragment(new HomeFragment());
        setTabState(1);

        // 5. Sự kiện Click chuyển Tab
        setupEvents();


        // Xin quyền thông báo (Android 13+)
        askNotificationPermission();

        // Cập nhật Token Firebase lên Server (Nếu đã đăng nhập)
        if (savedToken != null) {
            updateFcmTokenToServer();
        }
    }

    private void setupEvents() {
        btnHome.setOnClickListener(v -> {
            loadFragment(new HomeFragment());
            setTabState(1);
        });

        btnMap.setOnClickListener(v -> {
            loadFragment(new MapFragment());
            setTabState(2);
        });

        btnReport.setOnClickListener(v -> {
            if (!isLoggedIn()) {
                Toast.makeText(this, "Vui lòng đăng nhập để gửi phản ánh", Toast.LENGTH_SHORT).show();
                switchToTab(4);
                return;
            }
            Intent intent = new Intent(MainActivity.this, ReportIncidentActivity.class);
            startActivity(intent);
        });

        btnChat.setOnClickListener(v -> {
            loadFragment(new ChatFragment());
            setTabState(3);
        });

        btnProfile.setOnClickListener(v -> {
            loadFragment(new ProfileFragment());
            setTabState(4);
        });
    }

    // --- HÀM CẬP NHẬT FCM TOKEN ---
    private void updateFcmTokenToServer() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w("FCM", "Lỗi lấy token FCM", task.getException());
                        return;
                    }

                    // Lấy được token mới
                    String token = task.getResult();
                    Log.d("FCM", "Token của máy này: " + token);

                    // Gọi API gửi lên Server
                    ApiService api = ApiClient.getClient().create(ApiService.class);
                    // Lưu ý: Chuỗi token gửi lên phải bọc trong object hoặc gửi raw string tùy API quy định
                    // Ở đây giả sử gửi chuỗi raw string "token"
                    api.updateFcmToken(token).enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if(response.isSuccessful()) {
                                Log.d("FCM", "Đã cập nhật Token lên Server thành công");
                            } else {
                                Log.e("FCM", "Server trả lỗi: " + response.code());
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            Log.e("FCM", "Lỗi mạng khi cập nhật token: " + t.getMessage());
                        }
                    });
                });
    }

    // --- HÀM XIN QUYỀN THÔNG BÁO ---
    private void askNotificationPermission() {
        // Chỉ cần xin quyền với Android 13 (API 33) trở lên
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                // Hiển thị popup xin quyền
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    private boolean isLoggedIn() {
        SharedPreferences pref = getSharedPreferences("UserSession", MODE_PRIVATE);
        return pref.contains("TOKEN");
    }

    private void initViews() {
        btnHome = findViewById(R.id.btn_nav_home);
        btnMap = findViewById(R.id.btn_nav_map);
        btnChat = findViewById(R.id.btn_nav_chat);
        btnProfile = findViewById(R.id.btn_nav_profile);
        btnReport = findViewById(R.id.btn_nav_report);

        imgHome = findViewById(R.id.img_home);
        imgMap = findViewById(R.id.img_map);
        imgChat = findViewById(R.id.img_chat);
        imgProfile = findViewById(R.id.img_profile);

        tvHome = findViewById(R.id.tv_home);
        tvMap = findViewById(R.id.tv_map);
        tvChat = findViewById(R.id.tv_chat);
        tvProfile = findViewById(R.id.tv_profile);
    }

    private void loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frame_container, fragment)
                    .commit();
        }
    }

    private void setTabState(int tabIndex) {
        resetTabs();
        switch (tabIndex) {
            case 1:
                imgHome.setColorFilter(colorActive);
                tvHome.setTextColor(colorActive);
                break;
            case 2:
                imgMap.setColorFilter(colorActive);
                tvMap.setTextColor(colorActive);
                break;
            case 3:
                imgChat.setColorFilter(colorActive);
                tvChat.setTextColor(colorActive);
                break;
            case 4:
                imgProfile.setColorFilter(colorActive);
                tvProfile.setTextColor(colorActive);
                break;
        }
    }

    private void resetTabs() {
        imgHome.setColorFilter(colorInactive);
        imgMap.setColorFilter(colorInactive);
        imgChat.setColorFilter(colorInactive);
        imgProfile.setColorFilter(colorInactive);

        tvHome.setTextColor(colorInactive);
        tvMap.setTextColor(colorInactive);
        tvChat.setTextColor(colorInactive);
        tvProfile.setTextColor(colorInactive);
    }

    public void switchToTab(int tabIndex) {
        setTabState(tabIndex);
        switch (tabIndex) {
            case 1: loadFragment(new HomeFragment()); break;
            case 2: loadFragment(new MapFragment()); break;
            case 3: loadFragment(new ChatFragment()); break;
            case 4: loadFragment(new ProfileFragment()); break;
        }
    }
}