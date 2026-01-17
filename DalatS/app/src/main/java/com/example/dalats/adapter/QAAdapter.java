package com.example.dalats.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.dalats.R;
import com.example.dalats.activity.QuestionDetailActivity;
import com.example.dalats.model.QuestionResponseDTO;
import java.util.List;

public class QAAdapter extends RecyclerView.Adapter<QAAdapter.QAViewHolder> {
    private Context context;
    private List<QuestionResponseDTO> list;

    public QAAdapter(Context context, List<QuestionResponseDTO> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public QAViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_question, parent, false);
        return new QAViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QAViewHolder holder, int position) {
        QuestionResponseDTO item = list.get(position);

        holder.tvUser.setText(item.getUserName());
        holder.tvCategory.setText(item.getQuestionCategoryName());
        holder.tvContent.setText(item.getContent());

        int count = (item.getAnswers() != null) ? item.getAnswers().size() : 0;
        holder.tvCount.setText(count + " phản hồi");

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, QuestionDetailActivity.class);
            // Truyền toàn bộ object câu hỏi sang màn hình chi tiết
            intent.putExtra("QUESTION_DATA", item);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() { return list == null ? 0 : list.size(); }

    static class QAViewHolder extends RecyclerView.ViewHolder {
        TextView tvUser, tvCategory, tvContent, tvCount;
        public QAViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUser = itemView.findViewById(R.id.tv_qa_user);
            tvCategory = itemView.findViewById(R.id.tv_qa_category);
            tvContent = itemView.findViewById(R.id.tv_qa_content);
            tvCount = itemView.findViewById(R.id.tv_qa_count);
        }
    }
}