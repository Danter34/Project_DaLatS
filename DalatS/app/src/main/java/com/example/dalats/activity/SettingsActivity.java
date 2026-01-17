package com.example.dalats.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout; // Đã đổi từ TextView sang LinearLayout
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.dalats.R;
import com.example.dalats.api.ApiClient;
import com.example.dalats.model.ChangePasswordRequest;
import com.example.dalats.model.UpdateProfileRequest;
import okhttp3.ResponseBody;
import org.json.JSONObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SettingsActivity extends AppCompatActivity {

    private String currentName, currentEmail;

    // --- ĐÃ SỬA: Đổi kiểu dữ liệu thành LinearLayout ---
    private LinearLayout btnEditProfile, btnChangePass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        currentName = getIntent().getStringExtra("CURRENT_NAME");
        currentEmail = getIntent().getStringExtra("CURRENT_EMAIL");

        // Nút back (ImageView vẫn giữ nguyên)
        findViewById(R.id.btn_back_settings).setOnClickListener(v -> finish());

        // --- ĐÃ SỬA: Ánh xạ view vào biến LinearLayout ---
        btnEditProfile = findViewById(R.id.btn_edit_profile_sub);
        btnChangePass = findViewById(R.id.btn_change_pass_sub);

        btnEditProfile.setOnClickListener(v -> showEditProfileDialog());
        btnChangePass.setOnClickListener(v -> showChangePassDialog());
    }

    // --- Logic Dialog ---
    private void showEditProfileDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_edit_profile, null);
        builder.setView(view);

        EditText edtName = view.findViewById(R.id.edt_edit_name);
        EditText edtEmail = view.findViewById(R.id.edt_edit_email);
        Button btnSave = view.findViewById(R.id.btn_save_profile);

        edtName.setText(currentName);
        edtEmail.setText(currentEmail);

        AlertDialog dialog = builder.create();

        btnSave.setOnClickListener(v -> {
            String name = edtName.getText().toString();
            String email = edtEmail.getText().toString();
            btnSave.setEnabled(false);
            btnSave.setText("Đang lưu...");

            ApiClient.getAuthService().updateProfile(new UpdateProfileRequest(name, email)).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    btnSave.setEnabled(true);
                    btnSave.setText("LƯU");
                    if (response.isSuccessful()) {
                        try {
                            String raw = response.body().string();
                            String msg = raw;
                            try { msg = new JSONObject(raw).getString("message"); } catch (Exception e) {}
                            Toast.makeText(SettingsActivity.this, msg, Toast.LENGTH_LONG).show();

                            if (msg.toLowerCase().contains("xác minh")) {
                                performLogoutAndGoLogin();
                            } else {
                                currentName = name;
                                dialog.dismiss();
                            }
                        } catch (Exception e) {}
                    } else {
                        Toast.makeText(SettingsActivity.this, "Lỗi cập nhật", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    btnSave.setEnabled(true);
                    btnSave.setText("LƯU");
                    Toast.makeText(SettingsActivity.this, "Lỗi mạng", Toast.LENGTH_SHORT).show();
                }
            });
        });
        dialog.show();
    }

    private void showChangePassDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_change_password, null);
        builder.setView(view);

        EditText edtOld = view.findViewById(R.id.edt_old_pass);
        EditText edtNew = view.findViewById(R.id.edt_new_pass);
        Button btnSave = view.findViewById(R.id.btn_save_pass);
        AlertDialog dialog = builder.create();

        btnSave.setOnClickListener(v -> {
            String oldPass = edtOld.getText().toString();
            String newPass = edtNew.getText().toString();

            ChangePasswordRequest req = new ChangePasswordRequest(oldPass, newPass);
            ApiClient.getAuthService().changePassword(req).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(SettingsActivity.this, "Đổi mật khẩu thành công. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                        performLogoutAndGoLogin();
                    } else {
                        Toast.makeText(SettingsActivity.this, "Mật khẩu cũ không đúng", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(Call<Void> call, Throwable t) {}
            });
        });
        dialog.show();
    }

    private void performLogoutAndGoLogin() {
        SharedPreferences pref = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        pref.edit().clear().apply();
        ApiClient.setAuthToken(null);
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}