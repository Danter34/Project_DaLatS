package com.example.dalats.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log; // [Mới] Import Log
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.dalats.R;
import com.example.dalats.api.ApiClient;
import com.example.dalats.api.ApiService; // [Mới] Import Interface chứa hàm updateFcm
import com.example.dalats.model.AuthResponse;
import com.example.dalats.model.LoginRequest;
import com.google.firebase.messaging.FirebaseMessaging; // [Mới] Import Firebase

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private ImageView imgTogglePass;
    private boolean isPasswordVisible = false;
    private EditText edtEmail, edtPass;
    private Button btnLogin;
    private TextView tvRegister, tvForgotPass;

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
        imgTogglePass = findViewById(R.id.img_toggle_login_pass);
        tvForgotPass = findViewById(R.id.tv_forgot_pass);

        // Chuyển sang màn hình Đăng ký
        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });

        tvForgotPass.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
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

        // Xử lý ẩn/hiện mật khẩu
        imgTogglePass.setOnClickListener(v -> {
            if (isPasswordVisible) {
                edtPass.setTransformationMethod(PasswordTransformationMethod.getInstance());
                imgTogglePass.setColorFilter(Color.parseColor("#B2B2B2"));
            } else {
                edtPass.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                imgTogglePass.setColorFilter(Color.parseColor("#27AE60"));
            }
            isPasswordVisible = !isPasswordVisible;
            edtPass.setSelection(edtPass.length());
        });
    }

    private void performLogin(String email, String password) {
        LoginRequest request = new LoginRequest(email, password);

        // Gọi API Login
        ApiClient.getAuthService().login(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // 1. Lưu thông tin User
                    saveUserSession(response.body());
                    // 2. Cấu hình Token cho các request sau
                    ApiClient.setAuthToken(response.body().getToken());

                    // ============================================================
                    // [QUAN TRỌNG] GỬI FCM TOKEN LÊN SERVER NGAY LẬP TỨC
                    // ============================================================
                    FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            Log.w("Login", "Lỗi lấy FCM Token", task.getException());
                            return;
                        }

                        // Lấy Token FCM
                        String fcmToken = task.getResult();
                        Log.d("Login", "FCM Token: " + fcmToken);

                        // Gọi API cập nhật Token (Sử dụng ApiService chung hoặc tạo mới)
                        // Giả sử updateFcmToken nằm trong ApiService
                        ApiService api = ApiClient.getClient().create(ApiService.class);
                        api.updateFcmToken(fcmToken).enqueue(new Callback<Void>() {
                            @Override
                            public void onResponse(Call<Void> call, Response<Void> res) {
                                Log.d("Login", "Đã cập nhật FCM lên Server");
                            }

                            @Override
                            public void onFailure(Call<Void> call, Throwable t) {
                                Log.e("Login", "Lỗi mạng khi cập nhật FCM");
                            }
                        });
                    });
                    // ============================================================

                    Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                    goToMainActivity();

                } else {
                    // Xử lý lỗi từ server
                    String errorMsg = "Đăng nhập thất bại";
                    try {
                        if (response.errorBody() != null) {
                            errorMsg = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Lỗi kết nối server", Toast.LENGTH_SHORT).show();
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