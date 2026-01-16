package com.example.dalats.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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
import com.example.dalats.model.ChangePasswordRequest;
import com.example.dalats.model.UpdateProfileRequest;
import com.example.dalats.model.UserDashboardResponse;
import com.example.dalats.model.UserProfileResponse;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import org.json.JSONObject;
public class ProfileFragment extends Fragment {

    // View
    private LinearLayout layoutNotLogin;
    private NestedScrollView layoutLoggedIn;
    private Button btnLoginNow, btnLogout;
    private TextView tvFullName, tvEmail, tvAvatarChar;

    // Thống kê
    private TextView tvStatPending, tvStatProcessing, tvStatCompleted;

    // Menu chức năng
    private TextView btnEditProfile, btnChangePass;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        initViews(view);
        checkLoginStatus();

        // Events
        btnLoginNow.setOnClickListener(v -> startActivity(new Intent(getActivity(), LoginActivity.class)));
        btnLogout.setOnClickListener(v -> performLogout());

        btnEditProfile.setOnClickListener(v -> showEditProfileDialog());
        btnChangePass.setOnClickListener(v -> showChangePassDialog());

        return view;
    }

    private void initViews(View view) {
        layoutNotLogin = view.findViewById(R.id.layout_not_login);
        layoutLoggedIn = view.findViewById(R.id.layout_logged_in);
        btnLoginNow = view.findViewById(R.id.btn_login_now);
        btnLogout = view.findViewById(R.id.btn_logout);

        tvFullName = view.findViewById(R.id.tv_fullname);
        tvEmail = view.findViewById(R.id.tv_email);
        tvAvatarChar = view.findViewById(R.id.tv_avatar_char);

        tvStatPending = view.findViewById(R.id.tv_stat_pending);
        tvStatProcessing = view.findViewById(R.id.tv_stat_processing);
        tvStatCompleted = view.findViewById(R.id.tv_stat_completed);

        // Bạn cần đặt ID cho 2 TextView trong XML: id/btn_edit_profile và id/btn_change_pass
        btnEditProfile = view.findViewById(R.id.btn_edit_profile);
        btnChangePass = view.findViewById(R.id.btn_change_pass);
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

            // 1. Load Profile mới nhất từ API
            loadUserProfile();

            // 2. Load Dashboard (Thống kê)
            loadDashboard();
        } else {
            layoutNotLogin.setVisibility(View.VISIBLE);
            layoutLoggedIn.setVisibility(View.GONE);
        }
    }

    // --- API: Load Profile ---
    private void loadUserProfile() {
        ApiClient.getAuthService().getProfile().enqueue(new Callback<UserProfileResponse>() {
            @Override
            public void onResponse(Call<UserProfileResponse> call, Response<UserProfileResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserProfileResponse user = response.body();
                    tvFullName.setText(user.getFullName());
                    tvEmail.setText(user.getEmail());
                    if (!user.getFullName().isEmpty()) {
                        tvAvatarChar.setText(String.valueOf(user.getFullName().charAt(0)).toUpperCase());
                    }

                    // Lưu lại vào cache để dùng cho lần sau
                    SharedPreferences.Editor editor = getContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE).edit();
                    editor.putString("FULL_NAME", user.getFullName());
                    editor.putString("EMAIL", user.getEmail());
                    editor.apply();
                }
            }
            @Override
            public void onFailure(Call<UserProfileResponse> call, Throwable t) {}
        });
    }

    // --- API: Load Dashboard ---
    private void loadDashboard() {
        ApiClient.getAuthService().getDashboard().enqueue(new Callback<UserDashboardResponse>() {
            @Override
            public void onResponse(Call<UserDashboardResponse> call, Response<UserDashboardResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserDashboardResponse data = response.body();
                    tvStatPending.setText(String.valueOf(data.getPending()));
                    tvStatProcessing.setText(String.valueOf(data.getProcessing()));
                    tvStatCompleted.setText(String.valueOf(data.getCompleted()));
                }
            }
            @Override
            public void onFailure(Call<UserDashboardResponse> call, Throwable t) {}
        });
    }

    // --- DIALOG: Đổi mật khẩu ---
    private void showChangePassDialog() {
        if (getContext() == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_change_password, null);
        builder.setView(view);

        EditText edtOld = view.findViewById(R.id.edt_old_pass);
        EditText edtNew = view.findViewById(R.id.edt_new_pass);
        Button btnSave = view.findViewById(R.id.btn_save_pass);

        AlertDialog dialog = builder.create();

        btnSave.setOnClickListener(v -> {
            String oldPass = edtOld.getText().toString().trim();
            String newPass = edtNew.getText().toString().trim();

            if (oldPass.isEmpty() || newPass.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            // Khóa nút để tránh bấm nhiều lần
            btnSave.setEnabled(false);
            btnSave.setText("Đang xử lý...");

            ChangePasswordRequest req = new ChangePasswordRequest(oldPass, newPass);
            ApiClient.getAuthService().changePassword(req).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    btnSave.setEnabled(true);
                    btnSave.setText("XÁC NHẬN ĐỔI");

                    if (response.isSuccessful()) {
                        // 1. Thông báo
                        Toast.makeText(getContext(), "Đổi mật khẩu thành công. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();

                        // 2. Đóng dialog
                        dialog.dismiss();

                        // 3. TỰ ĐỘNG ĐĂNG XUẤT (Thêm dòng này)
                        performLogoutAndGoLogin();
                    } else {
                        Toast.makeText(getContext(), "Mật khẩu cũ không đúng", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    btnSave.setEnabled(true);
                    btnSave.setText("XÁC NHẬN ĐỔI");
                    Toast.makeText(getContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                }
            });
        });
        dialog.show();
    }

    // --- DIALOG: Sửa thông tin ---
    private void showEditProfileDialog() {
        if (getContext() == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_profile, null);
        builder.setView(view);

        EditText edtName = view.findViewById(R.id.edt_edit_name);
        EditText edtEmail = view.findViewById(R.id.edt_edit_email);
        Button btnSave = view.findViewById(R.id.btn_save_profile);

        // Điền sẵn dữ liệu hiện tại
        edtName.setText(tvFullName.getText());
        edtEmail.setText(tvEmail.getText());

        AlertDialog dialog = builder.create();

        btnSave.setOnClickListener(v -> {
            String name = edtName.getText().toString();
            String email = edtEmail.getText().toString();

            btnSave.setEnabled(false);
            btnSave.setText("Đang lưu...");

            UpdateProfileRequest req = new UpdateProfileRequest(name, email);

            ApiClient.getAuthService().updateProfile(req).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    btnSave.setEnabled(true);
                    btnSave.setText("LƯU THAY ĐỔI");

                    if (response.isSuccessful()) {
                        try {
                            String rawJson = response.body().string();
                            String cleanMessage = "";
                            try {
                                JSONObject jsonObject = new JSONObject(rawJson);
                                cleanMessage = jsonObject.getString("message");
                            } catch (Exception e) {
                                cleanMessage = rawJson;
                            }

                            Toast.makeText(getContext(), cleanMessage, Toast.LENGTH_LONG).show();

                            if (cleanMessage.toLowerCase().contains("xác minh") ||
                                    cleanMessage.toLowerCase().contains("kiểm tra email")) {
                                dialog.dismiss();
                                performLogoutAndGoLogin();
                            } else {
                                loadUserProfile();
                                dialog.dismiss();
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        Toast.makeText(getContext(), "Email đã tồn tại hoặc lỗi server", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    btnSave.setEnabled(true);
                    btnSave.setText("LƯU THAY ĐỔI");
                    Toast.makeText(getContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                }
            });
        });
        dialog.show();
    }
    private void performLogoutAndGoLogin() {
        if (getContext() == null) return;

        // 1. Xóa Token
        SharedPreferences pref = getContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        pref.edit().clear().apply();
        ApiClient.setAuthToken(null);

        // 2. Chuyển về màn hình Login và XÓA HẾT Activity cũ (để không back lại được)
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        // (Tùy chọn) Kết thúc Activity chứa Fragment này nếu cần
        if (getActivity() != null) getActivity().finish();
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