package com.example.dalats.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.dalats.R;
import com.example.dalats.model.CommentResponse;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {
    private List<CommentResponse> list;

    public CommentAdapter(List<CommentResponse> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CommentResponse c = list.get(position);
        holder.tvName.setText(c.getFullName());
        holder.tvContent.setText(c.getContent());
        holder.tvTime.setText(getRelativeTime(c.getCreatedAt()));
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvContent, tvTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_cmt_name);
            tvContent = itemView.findViewById(R.id.tv_cmt_content);
            tvTime = itemView.findViewById(R.id.tv_cmt_time);
        }
    }

    private String getRelativeTime(String rawDate) {
        if (rawDate == null) return "";

        try {
            SimpleDateFormat input =
                    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS", Locale.US);

            // FIX TIMEZONE TẠI ĐÂY
            input.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));

            Date date = input.parse(rawDate);
            if (date == null) return "";

            long now = System.currentTimeMillis();
            long diff = now - date.getTime();

            long minute = 60 * 1000;
            long hour = 60 * minute;
            long day = 24 * hour;
            long week = 7 * day;
            long month = 30 * day;
            long year = 365 * day;

            if (diff < minute) {
                return "Vừa xong";
            } else if (diff < hour) {
                return diff / minute + " phút trước";
            } else if (diff < day) {
                return diff / hour + " giờ trước";
            } else if (diff < week) {
                return diff / day + " ngày trước";
            } else if (diff < month) {
                return diff / week + " tuần trước";
            } else if (diff < year) {
                return diff / month + " tháng trước";
            } else {
                return diff / year + " năm trước";
            }

        } catch (Exception e) {
            return rawDate;
        }
    }
}