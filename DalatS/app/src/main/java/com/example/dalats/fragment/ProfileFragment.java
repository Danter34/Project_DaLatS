package com.example.dalats.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout; // Đã đổi từ TextView sang LinearLayout
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;

import com.example.dalats.R;
import com.example.dalats.activity.AppInfoActivity;
import com.example.dalats.activity.DashboardActivity;
import com.example.dalats.activity.LoginActivity;
import com.example.dalats.activity.SettingsActivity;
import com.example.dalats.api.ApiClient;
import com.example.dalats.model.UserProfileResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {

    private LinearLayout layoutNotLogin;
    private NestedScrollView layoutLoggedIn;
    private Button btnLoginNow;
    private TextView tvFullName, tvEmailHidden, tvAvatarChar;

    // --- ĐÃ SỬA: Đổi kiểu dữ liệu thành LinearLayout ---
    private LinearLayout btnSettings, btnActivityDashboard, btnAppInfo, btnLogout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        initViews(view);
        checkLoginStatus();

        // --- SỰ KIỆN CLICK (Vẫn dùng được cho LinearLayout) ---

        btnLoginNow.setOnClickListener(v -> startActivity(new Intent(getActivity(), LoginActivity.class)));

        // Mở Cài đặt
        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), SettingsActivity.class);
            // Truyền dữ liệu để điền sẵn vào form sửa thông tin
            intent.putExtra("CURRENT_NAME", tvFullName.getText().toString());
            intent.putExtra("CURRENT_EMAIL", tvEmailHidden.getText().toString());
            startActivity(intent);
        });

        // Mở Dashboard
        btnActivityDashboard.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), DashboardActivity.class));
        });

        // Mở App Info
        btnAppInfo.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), AppInfoActivity.class));
        });

        // Đăng xuất
        btnLogout.setOnClickListener(v -> performLogout());

        return view;
    }

    private void initViews(View view) {
        layoutNotLogin = view.findViewById(R.id.layout_not_login);
        layoutLoggedIn = view.findViewById(R.id.layout_logged_in);
        btnLoginNow = view.findViewById(R.id.btn_login_now);
        tvFullName = view.findViewById(R.id.tv_fullname);
        tvEmailHidden = view.findViewById(R.id.tv_email_hidden);
        tvAvatarChar = view.findViewById(R.id.tv_avatar_char);

        // --- ĐÃ SỬA: Ánh xạ view vào biến LinearLayout ---
        btnSettings = view.findViewById(R.id.btn_settings);
        btnActivityDashboard = view.findViewById(R.id.btn_activity_dashboard);
        btnAppInfo = view.findViewById(R.id.btn_app_info);
        btnLogout = view.findViewById(R.id.btn_logout);
    }

    @Override
    public void onResume() {
        super.onResume();
        checkLoginStatus();
    }

    private void checkLoginStatus() {
        if (getContext() == null) return;
        SharedPreferences pref = getContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        String token = pref.getString("TOKEN", null);

        if (token != null) {
            layoutNotLogin.setVisibility(View.GONE);
            layoutLoggedIn.setVisibility(View.VISIBLE);
            loadUserProfile();
        } else {
            layoutNotLogin.setVisibility(View.VISIBLE);
            layoutLoggedIn.setVisibility(View.GONE);
        }
    }

    private void loadUserProfile() {
        ApiClient.getAuthService().getProfile().enqueue(new Callback<UserProfileResponse>() {
            @Override
            public void onResponse(Call<UserProfileResponse> call, Response<UserProfileResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserProfileResponse user = response.body();
                    tvFullName.setText(user.getFullName());
                    tvEmailHidden.setText(user.getEmail());
                    if (!user.getFullName().isEmpty()) {
                        tvAvatarChar.setText(String.valueOf(user.getFullName().charAt(0)).toUpperCase());
                    }
                }
            }
            @Override
            public void onFailure(Call<UserProfileResponse> call, Throwable t) {}
        });
    }

    private void performLogout() {
        if (getContext() == null) return;
        SharedPreferences pref = getContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        pref.edit().clear().apply();
        ApiClient.setAuthToken(null);
        checkLoginStatus();
        Toast.makeText(getContext(), "Đã đăng xuất", Toast.LENGTH_SHORT).show();
    }
}