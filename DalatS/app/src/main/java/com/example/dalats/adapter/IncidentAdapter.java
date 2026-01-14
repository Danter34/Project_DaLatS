package com.example.dalats.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.dalats.R;
import com.example.dalats.model.Incident;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class IncidentAdapter extends RecyclerView.Adapter<IncidentAdapter.IncidentViewHolder> {

    private Context context;
    private List<Incident> incidentList;

    public IncidentAdapter(Context context, List<Incident> incidentList) {
        this.context = context;
        this.incidentList = incidentList;
    }

    @NonNull
    @Override
    public IncidentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_incident, parent, false);
        return new IncidentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IncidentViewHolder holder, int position) {
        Incident incident = incidentList.get(position);

        // 1. Category Styling (Pastel colors)
        holder.tvCategory.setText(incident.getCategoryName());

        int level = incident.getAlertLevel();
        int bgColor;
        int textColor;

        switch (level) {
            case 1: // VÀNG
                bgColor = Color.parseColor("#FFFDE7"); // vàng nhạt
                textColor = Color.parseColor("#F9A825"); // vàng đậm
                break;

            case 2: // CAM
                bgColor = Color.parseColor("#FFF3E0"); // cam nhạt
                textColor = Color.parseColor("#EF6C00"); // cam đậm
                break;
            case 3:
                bgColor = Color.parseColor("#FFEBEE");
                textColor = Color.parseColor("#C62828");
                break;
            default:
                bgColor = Color.parseColor("#F5F5F5");
                textColor = Color.parseColor("#616161");
        }

        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.RECTANGLE);
        shape.setCornerRadius(dpToPx(4));
        shape.setColor(bgColor);

        holder.tvCategory.setBackground(shape);
        holder.tvCategory.setTextColor(textColor);

        // 2. Title
        holder.tvTitle.setText(incident.getTitle());

        // 3. Date Formatting
        String rawDate = incident.getCreatedAt();
        if (rawDate != null) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
                Date date = inputFormat.parse(rawDate);
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                holder.tvTime.setText(outputFormat.format(date));
            } catch (Exception e) {
                if (rawDate.length() >= 10) {
                    holder.tvTime.setText(rawDate.substring(0, 10));
                } else {
                    holder.tvTime.setText("Vừa xong");
                }
            }
        } else {
            holder.tvTime.setText("Đang cập nhật");
        }

        // 4. Image Loading
        if (incident.getImages() != null && !incident.getImages().isEmpty()) {
            String path = incident.getImages().get(0).getFilePath();
            String fullUrl = "http://10.0.2.2:5084" + path;

            Glide.with(context)
                    .load(fullUrl)
                    .placeholder(R.drawable.ic_camera)
                    .error(R.drawable.ic_camera)
                    .centerCrop()
                    .into(holder.imgIncident);
        } else {
            holder.imgIncident.setImageResource(R.drawable.ic_camera);
        }
    }

    @Override
    public int getItemCount() {
        return incidentList != null ? incidentList.size() : 0;
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                context.getResources().getDisplayMetrics()
        );
    }

    public static class IncidentViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvCategory, tvTime;
        ImageView imgIncident;

        public IncidentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategory = itemView.findViewById(R.id.tv_category);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvTime = itemView.findViewById(R.id.tv_time);
            imgIncident = itemView.findViewById(R.id.img_incident);
        }
    }
}