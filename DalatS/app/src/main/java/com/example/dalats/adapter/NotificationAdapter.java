package com.example.dalats.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.dalats.R;
import com.example.dalats.api.ApiClient;
import com.example.dalats.model.NotificationDTO;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotiViewHolder> {
    private Context context;
    private List<NotificationDTO> list;

    public NotificationAdapter(Context context, List<NotificationDTO> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public NotiViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
        return new NotiViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotiViewHolder holder, int position) {
        NotificationDTO item = list.get(position);
        holder.tvMessage.setText(item.getMessage());
        holder.tvDate.setText(item.getCreatedAt().substring(0, 10)); // Cắt lấy ngày

        // Xử lý giao diện Đã đọc / Chưa đọc
        if (item.isRead()) {
            holder.viewUnreadDot.setVisibility(View.GONE);
            updateIconColor(holder.btnMarkRead, "#BDBDBD"); // Màu xám (Đã đọc)
        } else {
            holder.viewUnreadDot.setVisibility(View.VISIBLE);
            updateIconColor(holder.btnMarkRead, "#4CAF50"); // Màu xanh (Chưa đọc)
        }

        // Sự kiện click vào icon tròn để đánh dấu đã đọc
        holder.btnMarkRead.setOnClickListener(v -> {
            if (!item.isRead()) {
                // Gọi API đánh dấu đã đọc
                markAsRead(item.getNotificationId(), position);
            }
        });
    }

    private void markAsRead(int notiId, int position) {
        ApiClient.getNotificationService().markAsRead(notiId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    // Cập nhật trạng thái trong list và refresh item đó
                    list.get(position).setRead(true);
                    notifyItemChanged(position);
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {}
        });
    }

    private void updateIconColor(View view, String colorHex) {
        GradientDrawable bg = (GradientDrawable) view.getBackground();
        bg.setColor(Color.parseColor(colorHex));
    }

    @Override
    public int getItemCount() { return list == null ? 0 : list.size(); }

    static class NotiViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvDate;
        View viewUnreadDot;
        FrameLayout btnMarkRead;

        public NotiViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tv_noti_message);
            tvDate = itemView.findViewById(R.id.tv_noti_date);
            viewUnreadDot = itemView.findViewById(R.id.view_unread_dot);
            btnMarkRead = itemView.findViewById(R.id.btn_mark_read);
        }
    }
}