package com.example.dalats.activity;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.dalats.R;
import com.example.dalats.adapter.AnswerAdapter;
import com.example.dalats.model.QuestionResponseDTO;

public class QuestionDetailActivity extends AppCompatActivity {
    private TextView tvUser, tvContent, tvTime;
    private RecyclerView recyclerAnswers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_detail);

        tvUser = findViewById(R.id.tv_detail_user);
        tvContent = findViewById(R.id.tv_detail_content);
        tvTime = findViewById(R.id.tv_detail_time);
        recyclerAnswers = findViewById(R.id.recycler_answers);
        recyclerAnswers.setLayoutManager(new LinearLayoutManager(this));

        // Nhận dữ liệu từ Intent
        QuestionResponseDTO question = (QuestionResponseDTO) getIntent().getSerializableExtra("QUESTION_DATA");

        if (question != null) {
            tvUser.setText(question.getUserName());
            tvContent.setText(question.getContent());
            tvTime.setText(question.getCreatedAt());

            // Hiển thị danh sách câu trả lời
            if (question.getAnswers() != null && !question.getAnswers().isEmpty()) {
                AnswerAdapter adapter = new AnswerAdapter(this, question.getAnswers());
                recyclerAnswers.setAdapter(adapter);
            }
        }
    }
}