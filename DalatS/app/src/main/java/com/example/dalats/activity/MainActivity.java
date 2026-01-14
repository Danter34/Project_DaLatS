package com.example.dalats.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.dalats.R;
import com.example.dalats.fragment.HomeFragment;
import com.example.dalats.fragment.MapFragment;

public class MainActivity extends AppCompatActivity {

    // Khai báo View
    private LinearLayout btnHome, btnMap, btnChat, btnProfile, btnReport;
    private ImageView imgHome, imgMap, imgChat, imgProfile;
    private TextView tvHome, tvMap, tvChat, tvProfile;

    // Màu sắc (Lấy từ colors.xml hoặc mã hex)
    private int colorActive;
    private int colorInactive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Khởi tạo màu sắc
        // colorActive = ContextCompat.getColor(this, R.color.green_primary); // Nếu dùng colors.xml
        // colorInactive = ContextCompat.getColor(this, R.color.text_gray);

        // Hoặc dùng mã màu trực tiếp để test luôn:
        colorActive = Color.parseColor("#4CAF50"); // Màu xanh lá
        colorInactive = Color.parseColor("#757575"); // Màu xám

        // 2. Ánh xạ View
        initViews();

        // 3. Mặc định load Home
        loadFragment(new HomeFragment());
        setTabState(1); // 1 = Home

        // 4. Sự kiện Click
        btnHome.setOnClickListener(v -> {
            loadFragment(new HomeFragment());
            setTabState(1);
        });

        btnMap.setOnClickListener(v -> {
            loadFragment(new MapFragment());
            setTabState(2);
        });

        btnReport.setOnClickListener(v -> {
            // Nút giữa thường là hành động (chụp ảnh/gửi form)
            // Nếu muốn chuyển trang thì loadFragment(new ReportFragment());
            Toast.makeText(this, "Mở chức năng Phản ánh", Toast.LENGTH_SHORT).show();
        });

        btnChat.setOnClickListener(v -> { // Hỏi đáp
            // loadFragment(new ChatFragment());
            setTabState(3);
            Toast.makeText(this, "Hỏi đáp", Toast.LENGTH_SHORT).show();
        });

        btnProfile.setOnClickListener(v -> {
            // loadFragment(new ProfileFragment());
            setTabState(4);
            Toast.makeText(this, "Cá nhân", Toast.LENGTH_SHORT).show();
        });
    }

    private void initViews() {
        // Layout nút bấm
        btnHome = findViewById(R.id.btn_nav_home);
        btnMap = findViewById(R.id.btn_nav_map);
        btnChat = findViewById(R.id.btn_nav_chat);
        btnProfile = findViewById(R.id.btn_nav_profile);
        btnReport = findViewById(R.id.btn_nav_report);

        // Icon bên trong (Dùng ID mới thêm ở XML)
        imgHome = findViewById(R.id.img_home);
        imgMap = findViewById(R.id.img_map);
        imgChat = findViewById(R.id.img_chat);
        imgProfile = findViewById(R.id.img_profile);

        // Text bên trong (Dùng ID mới thêm ở XML)
        tvHome = findViewById(R.id.tv_home);
        tvMap = findViewById(R.id.tv_map);
        tvChat = findViewById(R.id.tv_chat);
        tvProfile = findViewById(R.id.tv_profile);
    }

    // Hàm chuyển Fragment
    private void loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frame_container, fragment)
                    .commit();
        }
    }

    // Hàm thay đổi màu sắc Tab
    private void setTabState(int tabIndex) {
        // Reset tất cả về màu Xám (Inactive)
        resetTabs();

        // Đổi màu nút được chọn thành Xanh (Active)
        switch (tabIndex) {
            case 1: // Home
                imgHome.setColorFilter(colorActive);
                tvHome.setTextColor(colorActive);
                break;
            case 2: // Map
                imgMap.setColorFilter(colorActive);
                tvMap.setTextColor(colorActive);
                break;
            case 3: // Chat
                imgChat.setColorFilter(colorActive);
                tvChat.setTextColor(colorActive);
                break;
            case 4: // Profile
                imgProfile.setColorFilter(colorActive);
                tvProfile.setTextColor(colorActive);
                break;
        }
    }

    private void resetTabs() {
        // Set màu xám cho icon
        imgHome.setColorFilter(colorInactive);
        imgMap.setColorFilter(colorInactive);
        imgChat.setColorFilter(colorInactive);
        imgProfile.setColorFilter(colorInactive);

        // Set màu xám cho text
        tvHome.setTextColor(colorInactive);
        tvMap.setTextColor(colorInactive);
        tvChat.setTextColor(colorInactive);
        tvProfile.setTextColor(colorInactive);
    }
    public void switchToTab(int tabIndex) {
        // Gọi lại logic chuyển tab đã viết trước đó
        setTabState(tabIndex);

        // Load Fragment tương ứng
        switch (tabIndex) {
            case 1: loadFragment(new HomeFragment()); break;
            case 2: loadFragment(new MapFragment()); break;
            // case 3: loadFragment(new ChatFragment()); break; // Bỏ comment khi có
            // case 4: loadFragment(new ProfileFragment()); break; // Bỏ comment khi có
        }
    }
}