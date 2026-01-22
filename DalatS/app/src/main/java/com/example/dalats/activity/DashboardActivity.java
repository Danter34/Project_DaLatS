package com.example.dalats.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.dalats.R;
import com.example.dalats.api.ApiClient;
import com.example.dalats.model.UserDashboardResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardActivity extends AppCompatActivity {


    private TextView tvPending, tvProcessing, tvCompleted, tvRejected;


    private TextView tvTrustScore, tvTrustStatus, tvQuotaText, tvQuotaPercent;
    private ProgressBar pbDailyQuota;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        initViews();

        // Nút back
        findViewById(R.id.btn_back_dashboard).setOnClickListener(v -> finish());

        loadData();
    }

    private void initViews() {
        // Ánh xạ thống kê
        tvPending = findViewById(R.id.tv_dash_pending);
        tvProcessing = findViewById(R.id.tv_dash_processing);
        tvCompleted = findViewById(R.id.tv_dash_completed);
        tvRejected = findViewById(R.id.tv_dash_rejected);

        // Ánh xạ Trust Score & Limit
        tvTrustScore = findViewById(R.id.tv_trust_score);
        tvTrustStatus = findViewById(R.id.tv_trust_status);
        tvQuotaText = findViewById(R.id.tv_quota_text);
        tvQuotaPercent = findViewById(R.id.tv_quota_percent);
        pbDailyQuota = findViewById(R.id.pb_daily_quota);
    }

    private void loadData() {
        ApiClient.getAuthService().getDashboard().enqueue(new Callback<UserDashboardResponse>() {
            @Override
            public void onResponse(Call<UserDashboardResponse> call, Response<UserDashboardResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserDashboardResponse data = response.body();

                    // 1. Cập nhật Thống kê
                    tvPending.setText(String.valueOf(data.getPending()));
                    tvProcessing.setText(String.valueOf(data.getProcessing()));
                    tvCompleted.setText(String.valueOf(data.getCompleted()));
                    tvRejected.setText(String.valueOf(data.getRejected()));

                    // 2. Cập nhật Điểm tin cậy
                    int score = data.getTrustScore();
                    tvTrustScore.setText(String.valueOf(score));
                    tvTrustStatus.setText(data.getTrustStatus());

                    // Logic đổi màu điểm số
                    if (score >= 50) {
                        tvTrustScore.setTextColor(Color.parseColor("#4CAF50")); // Xanh lá
                    } else if (score >= 0) {
                        tvTrustScore.setTextColor(Color.parseColor("#FF9800")); // Cam
                    } else {
                        tvTrustScore.setTextColor(Color.parseColor("#F44336")); // Đỏ (Bị khóa)
                    }

                    // 3. Cập nhật Hạn mức ngày
                    int used = data.getUsedDailyQuota();
                    int limit = data.getDailyReportLimit();

                    // Xử lý hiển thị text
                    if (limit > 0) {
                        tvQuotaText.setText("Đã dùng: " + used + "/" + limit + " lượt");
                        int percent = (int) (((float) used / limit) * 100);
                        tvQuotaPercent.setText(percent + "%");

                        pbDailyQuota.setMax(limit);
                        pbDailyQuota.setProgress(used);
                    } else {
                        // Trường hợp bị khóa (Limit = 0)
                        tvQuotaText.setText("Tài khoản đang bị hạn chế");
                        tvQuotaPercent.setText("0%");
                        pbDailyQuota.setMax(100);
                        pbDailyQuota.setProgress(100);

                    }
                }
            }

            @Override
            public void onFailure(Call<UserDashboardResponse> call, Throwable t) {
                Toast.makeText(DashboardActivity.this, "Lỗi tải thống kê", Toast.LENGTH_SHORT).show();
            }
        });
    }
}