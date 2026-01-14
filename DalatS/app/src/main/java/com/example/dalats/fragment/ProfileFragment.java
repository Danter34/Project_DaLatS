package com.example.dalats.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;

import com.example.dalats.R;
import com.example.dalats.activity.LoginActivity;
import com.example.dalats.api.ApiClient;

public class ProfileFragment extends Fragment {

    // View
    private LinearLayout layoutNotLogin;
    private NestedScrollView layoutLoggedIn;
    private Button btnLoginNow, btnLogout;
    private TextView tvFullName, tvEmail, tvAvatarChar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // 1. Ánh xạ View
        layoutNotLogin = view.findViewById(R.id.layout_not_login);
        layoutLoggedIn = view.findViewById(R.id.layout_logged_in);
        btnLoginNow = view.findViewById(R.id.btn_login_now);
        btnLogout = view.findViewById(R.id.btn_logout);
        tvFullName = view.findViewById(R.id.tv_fullname);
        tvEmail = view.findViewById(R.id.tv_email);
        tvAvatarChar = view.findViewById(R.id.tv_avatar_char); // Chữ cái đầu trong Avatar

        // 2. Kiểm tra trạng thái đăng nhập
        checkLoginStatus();

        // 3. Sự kiện Click Đăng nhập
        btnLoginNow.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
        });

        // 4. Sự kiện Click Đăng xuất
        btnLogout.setOnClickListener(v -> performLogout());

        return view;
    }

    // Hàm này chạy mỗi khi Fragment hiện lên (để cập nhật lại nếu vừa login xong)
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
            // --- ĐÃ ĐĂNG NHẬP ---
            layoutNotLogin.setVisibility(View.GONE);
            layoutLoggedIn.setVisibility(View.VISIBLE);

            // Lấy thông tin từ bộ nhớ ra hiển thị
            String name = pref.getString("FULL_NAME", "Người dùng");
            String email = pref.getString("EMAIL", "Chưa cập nhật");

            tvFullName.setText(name);
            tvEmail.setText(email);

            // Lấy chữ cái đầu của tên để làm Avatar
            if (!name.isEmpty()) {
                tvAvatarChar.setText(String.valueOf(name.charAt(0)).toUpperCase());
            }

        } else {
            // --- CHƯA ĐĂNG NHẬP ---
            layoutNotLogin.setVisibility(View.VISIBLE);
            layoutLoggedIn.setVisibility(View.GONE);
        }
    }

    private void performLogout() {
        if (getContext() == null) return;

        // 1. Xóa Token trong SharedPreferences
        SharedPreferences pref = getContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        pref.edit().clear().apply();

        // 2. Xóa Token trong ApiClient
        ApiClient.setAuthToken(null);

        // 3. Cập nhật lại giao diện
        checkLoginStatus();

        Toast.makeText(getContext(), "Đã đăng xuất", Toast.LENGTH_SHORT).show();
    }
}
