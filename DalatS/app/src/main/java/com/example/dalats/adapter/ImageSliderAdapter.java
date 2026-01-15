package com.example.dalats.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.dalats.R;
import com.example.dalats.activity.ImageDetailActivity;
import com.example.dalats.model.IncidentImage;

import java.util.ArrayList; // Nhớ import cái này
import java.util.List;

public class ImageSliderAdapter extends RecyclerView.Adapter<ImageSliderAdapter.ViewHolder> {
    private List<IncidentImage> images;

    // IP máy ảo: 10.0.2.2 | IP máy thật: 192.168.1.x
    private static final String BASE_URL = "http://10.0.2.2:5084";

    public ImageSliderAdapter(List<IncidentImage> images) {
        this.images = images;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_slider_image, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // 1. Logic hiển thị ảnh (như cũ)
        String path = images.get(position).getFilePath();
        if (path == null) return;

        String fullUrl;
        if (path.startsWith("http")) fullUrl = path;
        else fullUrl = BASE_URL + path;

        if (holder.img != null) {
            Glide.with(holder.itemView.getContext())
                    .load(fullUrl)
                    .placeholder(R.drawable.ic_camera)
                    .centerCrop()
                    .into(holder.img);
        }

        // 2. XỬ LÝ CLICK: Gửi danh sách ảnh đi để lướt được
        holder.itemView.setOnClickListener(v -> {
            Context context = holder.itemView.getContext();
            Intent intent = new Intent(context, ImageDetailActivity.class);

            // Tạo danh sách chứa tất cả link ảnh
            ArrayList<String> allUrls = new ArrayList<>();
            for (IncidentImage item : images) {
                String p = item.getFilePath();
                if (p != null) {
                    if (p.startsWith("http")) allUrls.add(p);
                    else allUrls.add(BASE_URL + p);
                }
            }

            // Gửi dữ liệu sang Activity chi tiết
            intent.putStringArrayListExtra("LIST_URL", allUrls); // Gửi list ảnh
            intent.putExtra("POSITION", position);               // Gửi vị trí ảnh đang bấm

            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() { return images == null ? 0 : images.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView img;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.img_slider);
        }
    }
}