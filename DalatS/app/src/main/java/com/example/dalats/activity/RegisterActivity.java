package com.example.dalats.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.dalats.R;
import com.example.dalats.api.ApiClient;
import com.example.dalats.model.RegisterRequest;

import okhttp3.ResponseBody; // Import quan trọng
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private EditText edtName, edtEmail, edtPass, edtRePass;
    private Button btnRegister;
    private TextView tvBack;
    private ImageView imgTogglePass, imgToggleRePass;
    private boolean isPassVisible = false;
    private boolean isRePassVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        edtName = findViewById(R.id.edt_fullname);
        edtEmail = findViewById(R.id.edt_reg_email);
        edtPass = findViewById(R.id.edt_reg_pass);
        edtRePass = findViewById(R.id.edt_reg_repass);
        btnRegister = findViewById(R.id.btn_register);
        tvBack = findViewById(R.id.tv_back);
        edtPass = findViewById(R.id.edt_reg_pass);
        edtRePass = findViewById(R.id.edt_reg_repass);
        imgTogglePass = findViewById(R.id.img_toggle_reg_pass);
        imgToggleRePass = findViewById(R.id.img_toggle_reg_repass);

        tvBack.setOnClickListener(v -> finish());

        btnRegister.setOnClickListener(v -> {
            String name = edtName.getText().toString().trim();
            String email = edtEmail.getText().toString().trim();
            String pass = edtPass.getText().toString().trim();
            String rePass = edtRePass.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            // Logic kiểm tra mật khẩu nhập lại (Chỉ xử lý ở Client, không gửi rePass lên API)
            if (!pass.equals(rePass)) {
                Toast.makeText(this, "Mật khẩu nhập lại không khớp", Toast.LENGTH_SHORT).show();
                return;
            }

            performRegister(name, email, pass);
        });
        // 1. Toggle Mật khẩu chính
        imgTogglePass.setOnClickListener(v -> {
            if (isPassVisible) {
                edtPass.setTransformationMethod(PasswordTransformationMethod.getInstance());
                imgTogglePass.setColorFilter(Color.parseColor("#B2B2B2"));
            } else {
                edtPass.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                imgTogglePass.setColorFilter(Color.parseColor("#27AE60"));
            }
            isPassVisible = !isPassVisible;
            edtPass.setSelection(edtPass.length());
        });

        // 2. Toggle Nhập lại mật khẩu
        imgToggleRePass.setOnClickListener(v -> {
            if (isRePassVisible) {
                edtRePass.setTransformationMethod(PasswordTransformationMethod.getInstance());
                imgToggleRePass.setColorFilter(Color.parseColor("#B2B2B2"));
            } else {
                edtRePass.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                imgToggleRePass.setColorFilter(Color.parseColor("#27AE60"));
            }
            isRePassVisible = !isRePassVisible;
            edtRePass.setSelection(edtRePass.length());
        });
    }

    private void performRegister(String name, String email, String pass) {
        RegisterRequest request = new RegisterRequest(name, email, pass);

        // Dùng Call<ResponseBody> thay vì Call<String>
        ApiClient.getAuthService().register(request).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    // API trả về 200 OK -> Đăng ký thành công
                    Toast.makeText(RegisterActivity.this, "Đăng ký thành công! Vui lòng đăng nhập.", Toast.LENGTH_LONG).show();
                    finish(); // Quay về màn hình Login
                } else {
                    // API trả về lỗi (400 Bad Request - Email đã tồn tại)
                    // Cần đọc nội dung lỗi từ errorBody
                    String errorMsg = "Đăng ký thất bại";
                    try {
                        if (response.errorBody() != null) {
                            errorMsg = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(RegisterActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                // Log lỗi ra để kiểm tra
                Log.e("RegisterError", "Error: " + t.getMessage());
                Toast.makeText(RegisterActivity.this, "Lỗi kết nối server", Toast.LENGTH_SHORT).show();
            }
        });
    }
}