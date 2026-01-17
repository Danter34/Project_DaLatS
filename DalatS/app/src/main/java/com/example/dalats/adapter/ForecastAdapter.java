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

        String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(item.dt * 1000));
        // Nếu là đầu ngày mới (00:00) hoặc item đầu tiên thì hiện Thứ (T2, T3..)
        if (time.equals("00:00") || position == 0) {
            time = new SimpleDateFormat("EE HH:mm", new Locale("vi", "VN")).format(new Date(item.dt * 1000));
        }
        holder.tvTime.setText(time);
        holder.tvTemp.setText(Math.round(item.main.temp) + "°");

        // Logic icon cơ bản
        String condition = item.weather.get(0).main.toLowerCase();
        int iconRes = R.drawable.tt; // Mặc định
        if (condition.contains("rain")) iconRes = R.drawable.rainy;
        else if (condition.contains("clear")) iconRes = R.drawable.sunny;
        else if (condition.contains("clouds")) iconRes = R.drawable.cloudy;

        holder.imgIcon.setImageResource(iconRes);
    }

    @Override
    public int getItemCount() { return list == null ? 0 : list.size(); }

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