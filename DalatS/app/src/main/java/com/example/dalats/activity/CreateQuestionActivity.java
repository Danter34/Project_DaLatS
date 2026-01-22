package com.example.dalats.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.dalats.R;
import com.example.dalats.api.ApiClient;
import com.example.dalats.model.CreateQuestionDTO;
import com.example.dalats.model.QuestionCategory;
import com.example.dalats.model.QuestionResponseDTO;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateQuestionActivity extends AppCompatActivity {
    private Spinner spinnerCategory;
    private EditText edtContent;
    private Button btnSend;
    private List<QuestionCategory> categories = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        if (!isLoggedIn()) {
            Toast.makeText(this, "Vui lòng đăng nhập để đặt câu hỏi", Toast.LENGTH_SHORT).show();


            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);


            finish();
            return;
        }

        setContentView(R.layout.activity_create_question);

        spinnerCategory = findViewById(R.id.spinner_qa_category);
        edtContent = findViewById(R.id.edt_qa_content);
        btnSend = findViewById(R.id.btn_send_question);
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        loadCategories();

        btnSend.setOnClickListener(v -> submitQuestion());
    }

    // --- HÀM KIỂM TRA TOKEN ---
    private boolean isLoggedIn() {
        SharedPreferences pref = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        // Kiểm tra xem có token không
        return pref.getString("TOKEN", null) != null;
    }

    private void loadCategories() {
        ApiClient.getQAService().getCategories().enqueue(new Callback<List<QuestionCategory>>() {
            @Override
            public void onResponse(Call<List<QuestionCategory>> call, Response<List<QuestionCategory>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    categories = response.body();

                    if (categories.isEmpty()) {
                        Toast.makeText(CreateQuestionActivity.this, "Không có danh mục nào", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    ArrayAdapter<QuestionCategory> adapter = new ArrayAdapter<>(
                            CreateQuestionActivity.this,
                            android.R.layout.simple_spinner_item,
                            categories);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerCategory.setAdapter(adapter);
                }
            }
            @Override
            public void onFailure(Call<List<QuestionCategory>> call, Throwable t) {
                Toast.makeText(CreateQuestionActivity.this, "Lỗi tải danh mục", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void submitQuestion() {
        String content = edtContent.getText().toString().trim();

        if (content.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập nội dung", Toast.LENGTH_SHORT).show();
            return;
        }

        if (categories.isEmpty()) {
            Toast.makeText(this, "Đang tải danh mục, vui lòng chờ...", Toast.LENGTH_SHORT).show();
            loadCategories();
            return;
        }

        QuestionCategory selected = (QuestionCategory) spinnerCategory.getSelectedItem();

        if (selected == null) {
            Toast.makeText(this, "Vui lòng chọn danh mục", Toast.LENGTH_SHORT).show();
            return;
        }

        CreateQuestionDTO dto = new CreateQuestionDTO(content, selected.getCategoryId());

        btnSend.setEnabled(false);
        btnSend.setText("Đang gửi...");

        ApiClient.getQAService().createQuestion(dto).enqueue(new Callback<QuestionResponseDTO>() {
            @Override
            public void onResponse(Call<QuestionResponseDTO> call, Response<QuestionResponseDTO> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(CreateQuestionActivity.this, "Đã gửi câu hỏi", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    btnSend.setEnabled(true);
                    btnSend.setText("GỬI CÂU HỎI");
                    // Kiểm tra lỗi 401 (Unauthorized)
                    if (response.code() == 401) {
                        Toast.makeText(CreateQuestionActivity.this, "Phiên đăng nhập hết hạn", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(CreateQuestionActivity.this, LoginActivity.class));
                        finish();
                    } else {
                        Toast.makeText(CreateQuestionActivity.this, "Lỗi gửi: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
            @Override
            public void onFailure(Call<QuestionResponseDTO> call, Throwable t) {
                btnSend.setEnabled(true);
                btnSend.setText("GỬI CÂU HỎI");
                Toast.makeText(CreateQuestionActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }
}