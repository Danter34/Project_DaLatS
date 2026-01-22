package com.example.dalats.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.TypedValue; // Import thêm cái này để tính dp
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.dalats.R;
import com.example.dalats.adapter.CommentAdapter;
import com.example.dalats.adapter.ImageSliderAdapter;
import com.example.dalats.api.ApiClient;
import com.example.dalats.model.CommentRequest;
import com.example.dalats.model.CommentResponse;
import com.example.dalats.model.Incident;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class IncidentDetailActivity extends AppCompatActivity {

    private int incidentId;

    // UI Elements
    private ViewPager2 viewPagerImages;
    private TextView tvTitle, tvTime, tvAddress, tvDesc, tvCategory, tvNoComment, tvLoginRequired;
    private RecyclerView rcvComments;
    private LinearLayout layoutInputComment;
    private EditText edtComment;
    private ImageView btnSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incident_detail);

        incidentId = getIntent().getIntExtra("INCIDENT_ID", -1);
        if (incidentId == -1) { finish(); return; }

        initViews();
        checkLoginState();
        loadDetail();
        loadComments();

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        btnSend.setOnClickListener(v -> postComment());
        tvLoginRequired.setOnClickListener(v -> startActivity(new Intent(this, LoginActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkLoginState();
    }

    private void initViews() {
        viewPagerImages = findViewById(R.id.viewPagerImages);
        tvTitle = findViewById(R.id.tv_title);
        tvTime = findViewById(R.id.tv_time);
        tvAddress = findViewById(R.id.tv_address);
        tvDesc = findViewById(R.id.tv_description);
        tvCategory = findViewById(R.id.tv_category);
        rcvComments = findViewById(R.id.rcv_comments);
        tvNoComment = findViewById(R.id.tv_no_comment);
        layoutInputComment = findViewById(R.id.layout_input_comment);
        edtComment = findViewById(R.id.edt_comment);
        btnSend = findViewById(R.id.btn_send);
        tvLoginRequired = findViewById(R.id.tv_login_required);

        rcvComments.setLayoutManager(new LinearLayoutManager(this));
    }

    private void checkLoginState() {
        SharedPreferences pref = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        boolean isLogged = pref.contains("TOKEN");
        layoutInputComment.setVisibility(isLogged ? View.VISIBLE : View.GONE);
        tvLoginRequired.setVisibility(isLogged ? View.GONE : View.VISIBLE);
    }

    private void loadDetail() {
        ApiClient.getIncidentService().getDetail(incidentId).enqueue(new Callback<Incident>() {
            @Override
            public void onResponse(Call<Incident> call, Response<Incident> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Incident data = response.body();

                    tvTitle.setText(data.getTitle());

                    // Format lại ngày tháng
                    tvTime.setText(formatDate(data.getCreatedAt()));

                    tvAddress.setText(data.getFullAddress());
                    tvDesc.setText(data.getDescription());
                    tvCategory.setText(data.getCategoryName());

                    // Cập nhật màu sắc giống Adapter
                    updateTagColor(data.getAlertLevel());

                    if (data.getImages() != null && !data.getImages().isEmpty()) {
                        ImageSliderAdapter adapter = new ImageSliderAdapter(data.getImages());
                        viewPagerImages.setAdapter(adapter);
                    }
                }
            }
            @Override
            public void onFailure(Call<Incident> call, Throwable t) {}
        });
    }


    private void updateTagColor(int level) {
        int bgColor, textColor;

        switch (level) {
            case 1: // VÀNG
                bgColor = Color.parseColor("#FFFDE7");
                textColor = Color.parseColor("#F9A825");
                break;
            case 2: // CAM
                bgColor = Color.parseColor("#FFF3E0");
                textColor = Color.parseColor("#EF6C00");
                break;
            case 3: // ĐỎ
                bgColor = Color.parseColor("#FFEBEE");
                textColor = Color.parseColor("#C62828");
                break;
            default:
                bgColor = Color.parseColor("#F5F5F5");
                textColor = Color.parseColor("#616161");
        }

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(bgColor);
        bg.setCornerRadius(dpToPx(4)); // Bo góc 4dp

        tvCategory.setBackground(bg);
        tvCategory.setTextColor(textColor);
    }

    // --- LOGIC FORMAT NGÀY ---
    private String formatDate(String rawDate) {
        if (rawDate == null) return "Đang cập nhật";
        try {
            // Định dạng đầu vào từ API (.NET)
            SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
            Date date = input.parse(rawDate);

            // Định dạng đầu ra: Chỉ hiện Ngày/Tháng/Năm
            SimpleDateFormat output = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            return output.format(date);
        } catch (Exception e) {
            return rawDate;
        }
    }

    // Tiện ích đổi dp sang px
    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }


    private void loadComments() {
        ApiClient.getIncidentService().getComments(incidentId).enqueue(new Callback<List<CommentResponse>>() {
            @Override
            public void onResponse(Call<List<CommentResponse>> call, Response<List<CommentResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<CommentResponse> list = response.body();
                    if (list.isEmpty()) {
                        tvNoComment.setVisibility(View.VISIBLE);
                        rcvComments.setVisibility(View.GONE);
                    } else {
                        tvNoComment.setVisibility(View.GONE);
                        rcvComments.setVisibility(View.VISIBLE);
                        CommentAdapter adapter = new CommentAdapter(list);
                        rcvComments.setAdapter(adapter);
                    }
                }
            }
            @Override
            public void onFailure(Call<List<CommentResponse>> call, Throwable t) {}
        });
    }

    private void postComment() {
        String content = edtComment.getText().toString().trim();
        if (content.isEmpty()) return;
        CommentRequest req = new CommentRequest(content);
        ApiClient.getIncidentService().postComment(incidentId, req).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    edtComment.setText("");
                    loadComments();
                    Toast.makeText(IncidentDetailActivity.this, "Đã gửi!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(IncidentDetailActivity.this, "Lỗi gửi", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(IncidentDetailActivity.this, "Lỗi mạng", Toast.LENGTH_SHORT).show();
            }
        });
    }
}