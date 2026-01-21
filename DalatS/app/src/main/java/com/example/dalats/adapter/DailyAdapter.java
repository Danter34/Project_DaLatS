package com.example.dalats.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.dalats.R;
import com.example.dalats.model.DailyWeather;
import java.util.List;

public class DailyAdapter extends RecyclerView.Adapter<DailyAdapter.DailyViewHolder> {
    private List<DailyWeather> list;

    public DailyAdapter(List<DailyWeather> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public DailyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_daily_forecast, parent, false);
        return new DailyViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull DailyViewHolder holder, int position) {
        DailyWeather item = list.get(position);
        holder.tvDayName.setText(item.dayName);

        // [SỬA LẠI] Luôn hiển thị % mưa
        holder.tvPop.setText(item.pop + "%");

        // Nếu tỉ lệ mưa > 0 thì hiện đậm/xanh, nếu 0% thì làm mờ đi cho đẹp (giống Samsung)
        if (item.pop > 0) {
            holder.tvPop.setAlpha(1.0f); // Hiện rõ
            holder.imgDrop.setAlpha(1.0f);
            holder.tvPop.setVisibility(View.VISIBLE);
            holder.imgDrop.setVisibility(View.VISIBLE);
        } else {
            // Cách 1: Vẫn hiện nhưng mờ đi
            holder.tvPop.setAlpha(0.3f);
            holder.imgDrop.setAlpha(0.3f);

        }

        holder.tvMax.setText(Math.round(item.maxTemp) + "°");
        holder.tvMin.setText(Math.round(item.minTemp) + "°");

        // Logic icon (Giống ForecastAdapter)
        String condition = item.icon.toLowerCase();
        int iconRes = R.drawable.cloudy;
        if (condition.contains("rain")) iconRes = R.drawable.rain;
        else if (condition.contains("clear")) iconRes = R.drawable.sunny;
        else if (condition.contains("thunder")) iconRes = R.drawable.thunder;

        holder.imgIcon.setImageResource(iconRes);
    }

    @Override
    public int getItemCount() { return list == null ? 0 : list.size(); }

    static class DailyViewHolder extends RecyclerView.ViewHolder {
        TextView tvDayName, tvPop, tvMax, tvMin;
        ImageView imgIcon;
        ImageView  imgDrop;
        public DailyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDayName = itemView.findViewById(R.id.tv_day_name);
            tvPop = itemView.findViewById(R.id.tv_pop);
            tvMax = itemView.findViewById(R.id.tv_daily_max);
            tvMin = itemView.findViewById(R.id.tv_daily_min);
            imgIcon = itemView.findViewById(R.id.img_daily_icon);
            imgDrop = itemView.findViewById(R.id.img_drop);
        }
    }
}