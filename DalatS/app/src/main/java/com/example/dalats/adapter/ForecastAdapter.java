package com.example.dalats.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.dalats.R;
import com.example.dalats.model.ForecastResponse;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.FViewHolder> {
    private List<ForecastResponse.ForecastItem> list;

    public ForecastAdapter(List<ForecastResponse.ForecastItem> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public FViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_forecast_glass, parent, false);
        return new FViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FViewHolder holder, int position) {
        ForecastResponse.ForecastItem item = list.get(position);

        // [CẬP NHẬT] Định dạng hiển thị: Thứ + Giờ (VD: T2 14:00) để dễ phân biệt các ngày
        // EEE: Thứ (Mon/Tue...), HH:mm: Giờ
        String time = new SimpleDateFormat("EEE HH:mm", new Locale("vi", "VN")).format(new Date(item.dt * 1000));

        // Viết hoa chữ cái đầu (tùy chọn)
        time = time.substring(0, 1).toUpperCase() + time.substring(1);

        holder.tvTime.setText(time);
        holder.tvTemp.setText(Math.round(item.main.temp) + "°");

        // Logic icon giữ nguyên như cũ
        String condition = item.weather.get(0).main.toLowerCase();
        int iconRes;

        if (condition.contains("rain") || condition.contains("drizzle")) {
            iconRes = R.drawable.rain;
        } else if (condition.contains("thunder")) {
            iconRes = R.drawable.thunder;
        } else if (condition.contains("clear")) {
            iconRes = R.drawable.sunny;
        } else if (condition.contains("cloud")) {
            iconRes = R.drawable.cloudy;
        } else {
            iconRes = R.drawable.cloudy;
        }

        holder.imgIcon.setImageResource(iconRes);
    }

    @Override
    public int getItemCount() {
        // Giới hạn hiển thị khoảng 8-10 mốc thời gian thôi cho đẹp (24h tới)
        if (list == null) return 0;
        return list.size();
    }

    static class FViewHolder extends RecyclerView.ViewHolder {
        TextView tvTime, tvTemp;
        ImageView imgIcon;
        public FViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tv_item_time);
            tvTemp = itemView.findViewById(R.id.tv_item_temp);
            imgIcon = itemView.findViewById(R.id.img_item_icon);
        }
    }
}