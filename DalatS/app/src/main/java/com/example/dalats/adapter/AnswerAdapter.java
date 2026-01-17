package com.example.dalats.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.dalats.R;
import com.example.dalats.model.AnswerResponseDTO;
import java.util.List;

public class AnswerAdapter extends RecyclerView.Adapter<AnswerAdapter.AnswerViewHolder> {
    private Context context;
    private List<AnswerResponseDTO> list;

    public AnswerAdapter(Context context, List<AnswerResponseDTO> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public AnswerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_answer, parent, false);
        return new AnswerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AnswerViewHolder holder, int position) {
        AnswerResponseDTO item = list.get(position);
        holder.tvResponder.setText(item.getResponderName() + " (" + item.getDepartmentName() + ")");
        holder.tvContent.setText(item.getContent());
        holder.tvDate.setText(item.getCreatedAt().substring(0, 10));
    }

    @Override
    public int getItemCount() { return list == null ? 0 : list.size(); }

    static class AnswerViewHolder extends RecyclerView.ViewHolder {
        TextView tvResponder, tvContent, tvDate;
        public AnswerViewHolder(@NonNull View itemView) {
            super(itemView);
            tvResponder = itemView.findViewById(R.id.tv_ans_responder);
            tvContent = itemView.findViewById(R.id.tv_ans_content);
            tvDate = itemView.findViewById(R.id.tv_ans_date);
        }
    }
}