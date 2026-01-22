package com.example.dalats.activity;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;
import androidx.appcompat.app.AppCompatActivity;
import com.example.dalats.R;
import com.example.dalats.api.ApiClient;
import com.example.dalats.model.CheckResetCodeRequest;
import com.example.dalats.model.ForgotPasswordRequest;
import com.example.dalats.model.ResetPasswordRequest;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgotPasswordActivity extends AppCompatActivity {

    private ViewFlipper viewFlipper;
    private ImageView btnBack;


    private EditText edtEmail;
    private Button btnSendCode;


    private EditText edtOtp;
    private TextView tvTimer, tvResendCode, tvOtpMessage;
    private Button btnVerifyCode;
    private CountDownTimer countDownTimer;


    private EditText edtNewPass, edtConfirmPass;
    private Button btnResetPass;


    private String currentEmail = "";
    private String currentCode = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        initViews();

        btnBack.setOnClickListener(v -> {

            if (viewFlipper.getDisplayedChild() == 0) finish();
            else {
                viewFlipper.showPrevious();
                if (countDownTimer != null) countDownTimer.cancel();
            }
        });


        btnSendCode.setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập email", Toast.LENGTH_SHORT).show();
                return;
            }
            sendForgotPassword(email);
        });


        btnVerifyCode.setOnClickListener(v -> {
            String otp = edtOtp.getText().toString().trim();
            if (otp.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập mã OTP", Toast.LENGTH_SHORT).show();
                return;
            }
            verifyResetCode(otp);
        });

        // Gửi lại mã
        tvResendCode.setOnClickListener(v -> {
            sendForgotPassword(currentEmail); // Gửi lại
            tvResendCode.setVisibility(View.GONE); // Ẩn nút gửi lại
            edtOtp.setText("");
        });


        btnResetPass.setOnClickListener(v -> {
            String pass = edtNewPass.getText().toString().trim();
            String confirm = edtConfirmPass.getText().toString().trim();

            if (pass.isEmpty() || confirm.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!pass.equals(confirm)) {
                Toast.makeText(this, "Mật khẩu nhập lại không khớp", Toast.LENGTH_SHORT).show();
                return;
            }
            performResetPassword(pass);
        });
    }

    private void initViews() {
        viewFlipper = findViewById(R.id.view_flipper);
        btnBack = findViewById(R.id.btn_back);


        edtEmail = findViewById(R.id.edt_forgot_email);
        btnSendCode = findViewById(R.id.btn_send_code);


        edtOtp = findViewById(R.id.edt_otp_code);
        tvTimer = findViewById(R.id.tv_timer);
        tvResendCode = findViewById(R.id.tv_resend_code);
        tvOtpMessage = findViewById(R.id.tv_otp_message);
        btnVerifyCode = findViewById(R.id.btn_verify_code);


        edtNewPass = findViewById(R.id.edt_new_pass);
        edtConfirmPass = findViewById(R.id.edt_confirm_pass);
        btnResetPass = findViewById(R.id.btn_reset_pass);
    }


    private void sendForgotPassword(String email) {
        btnSendCode.setEnabled(false);
        btnSendCode.setText("Đang gửi...");

        ApiClient.getAuthService().forgotPassword(new ForgotPasswordRequest(email)).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                btnSendCode.setEnabled(true);
                btnSendCode.setText("GỬI MÃ XÁC MINH");

                if (response.isSuccessful()) {
                    currentEmail = email; // Lưu email lại
                    tvOtpMessage.setText("Mã xác minh đã được gửi đến " + email);


                    viewFlipper.setDisplayedChild(1);

                    // Bắt đầu đếm ngược
                    startTimer();
                } else {
                    Toast.makeText(ForgotPasswordActivity.this, "Email không tồn tại hoặc lỗi server", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                btnSendCode.setEnabled(true);
                btnSendCode.setText("GỬI MÃ XÁC MINH");
                Toast.makeText(ForgotPasswordActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // API 2: Kiểm tra mã OTP
    private void verifyResetCode(String code) {
        ApiClient.getAuthService().checkResetCode(new CheckResetCodeRequest(currentEmail, code)).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    currentCode = code; // Lưu code lại
                    Toast.makeText(ForgotPasswordActivity.this, "Mã hợp lệ", Toast.LENGTH_SHORT).show();

                    // Dừng timer
                    if (countDownTimer != null) countDownTimer.cancel();

                    // Chuyển sang Slide 3 (Nhập Pass mới)
                    viewFlipper.setDisplayedChild(2);
                } else {
                    Toast.makeText(ForgotPasswordActivity.this, "Mã xác minh không đúng hoặc hết hạn", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {}
        });
    }

    // API 3: Đổi mật khẩu
    private void performResetPassword(String newPass) {
        ApiClient.getAuthService().resetPassword(new ResetPasswordRequest(currentEmail, currentCode, newPass)).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ForgotPasswordActivity.this, "Đổi mật khẩu thành công! Vui lòng đăng nhập.", Toast.LENGTH_LONG).show();
                    finish(); // Đóng Activity, quay về Login
                } else {
                    Toast.makeText(ForgotPasswordActivity.this, "Lỗi: Hết hạn phiên làm việc", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {}
        });
    }

    // HÀM ĐẾM NGƯỢC 2 PHÚT (120,000 ms)
    private void startTimer() {
        if (countDownTimer != null) countDownTimer.cancel();

        tvTimer.setVisibility(View.VISIBLE);
        tvResendCode.setVisibility(View.GONE);

        countDownTimer = new CountDownTimer(120000, 1000) {
            public void onTick(long millisUntilFinished) {
                // Format thời gian 02:00 -> 01:59...
                long minutes = (millisUntilFinished / 1000) / 60;
                long seconds = (millisUntilFinished / 1000) % 60;
                String timeFormatted = String.format("%02d:%02d", minutes, seconds);
                tvTimer.setText(timeFormatted);
            }

            public void onFinish() {
                tvTimer.setText("00:00");
                tvTimer.setVisibility(View.GONE); // Ẩn đồng hồ
                tvResendCode.setVisibility(View.VISIBLE); // Hiện nút gửi lại
            }
        }.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) countDownTimer.cancel();
    }
}