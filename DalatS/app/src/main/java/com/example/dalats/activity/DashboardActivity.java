package com.example.dalats.activity;

import android.os.Bundle;
import android.view.View;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Ánh xạ View
        tvPending = findViewById(R.id.tv_dash_pending);
        tvProcessing = findViewById(R.id.tv_dash_processing);
        tvCompleted = findViewById(R.id.tv_dash_completed);
        tvRejected = findViewById(R.id.tv_dash_rejected);

        // Nút back
        findViewById(R.id.btn_back_dashboard).setOnClickListener(v -> finish());

        loadData();
    }

    private void loadData() {
        ApiClient.getAuthService().getDashboard().enqueue(new Callback<UserDashboardResponse>() {
            @Override
            public void onResponse(Call<UserDashboardResponse> call, Response<UserDashboardResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserDashboardResponse data = response.body();
                    tvPending.setText(String.valueOf(data.getPending()));
                    tvProcessing.setText(String.valueOf(data.getProcessing()));
                    tvCompleted.setText(String.valueOf(data.getCompleted()));
                    tvRejected.setText(String.valueOf(data.getRejected()));

                }
            }

            @Override
            public void onFailure(Call<UserDashboardResponse> call, Throwable t) {
                Toast.makeText(DashboardActivity.this, "Lỗi tải thống kê", Toast.LENGTH_SHORT).show();
            }
        });
    }
}