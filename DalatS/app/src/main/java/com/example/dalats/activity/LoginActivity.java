package com.example.dalats.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.dalats.R;
import com.example.dalats.api.ApiClient;
import com.example.dalats.model.AuthResponse;
import com.example.dalats.model.LoginRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private ImageView imgTogglePass;
    private boolean isPasswordVisible = false;
    private EditText edtEmail, edtPass;
    private Button btnLogin;
    private TextView tvRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Kiểm tra nếu đã đăng nhập trước đó -> Vào thẳng Main
        if (isLoggedIn()) {
            goToMainActivity();
            return;
        }

        setContentView(R.layout.activity_login);

        // Ánh xạ
        edtEmail = findViewById(R.id.edt_email);
        edtPass = findViewById(R.id.edt_password);
        btnLogin = findViewById(R.id.btn_login);
        tvRegister = findViewById(R.id.tv_register);
        edtPass = findViewById(R.id.edt_password);
        imgTogglePass = findViewById(R.id.img_toggle_login_pass);
        // Chuyển sang màn hình Đăng ký
        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });

        // Xử lý nút Đăng nhập
        btnLogin.setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();
            String pass = edtPass.getText().toString().trim();

            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập email và mật khẩu", Toast.LENGTH_SHORT).show();
            } else {
                performLogin(email, pass);
            }
        });
        //xử lý ẩn mật khẩu
        imgTogglePass.setOnClickListener(v -> {
            if (isPasswordVisible) {
                // Đang hiện -> Ẩn đi
                edtPass.setTransformationMethod(PasswordTransformationMethod.getInstance());
                imgTogglePass.setColorFilter(Color.parseColor("#B2B2B2")); // Màu xám nhạt
            } else {
                // Đang ẩn -> Hiện lên
                edtPass.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                imgTogglePass.setColorFilter(Color.parseColor("#27AE60")); // Màu xanh (hoặc màu active)
            }
            isPasswordVisible = !isPasswordVisible;

            // Đưa con trỏ về cuối dòng
            edtPass.setSelection(edtPass.length());
        });
    }

    private void performLogin(String email, String password) {
        LoginRequest request = new LoginRequest(email, password);

        ApiClient.getAuthService().login(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // 1. Lưu Token và thông tin User
                    saveUserSession(response.body());

                    // 2. Cập nhật Token cho ApiClient ngay lập tức
                    ApiClient.setAuthToken(response.body().getToken());

                    Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                    goToMainActivity();
                } else {
                    Toast.makeText(LoginActivity.this, "Sai email hoặc mật khẩu", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveUserSession(AuthResponse data) {
        SharedPreferences pref = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        editor.putString("TOKEN", data.getToken());
        if (data.getUser() != null) {
            editor.putInt("USER_ID", data.getUser().getUserId());
            editor.putString("FULL_NAME", data.getUser().getFullName());
            editor.putString("EMAIL", data.getUser().getEmail());
            editor.putString("ROLE", data.getUser().getRole());
        }
        editor.apply();
    }

    private boolean isLoggedIn() {
        SharedPreferences pref = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        return pref.contains("TOKEN");
    }

    private void goToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}