package com.example.dalats.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.dalats.R;
import com.github.chrisbanes.photoview.PhotoView; // Thư viện Zoom
import java.util.List;

public class FullScreenImageAdapter extends RecyclerView.Adapter<FullScreenImageAdapter.ViewHolder> {
    private List<String> imageUrls;

    public FullScreenImageAdapter(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Layout item_photo_view.xml (tạo ở bước 4)
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_photo_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String url = imageUrls.get(position);
        Glide.with(holder.itemView.getContext())
                .load(url)
                .into(holder.photoView);
    }

    @Override
    public int getItemCount() { return imageUrls == null ? 0 : imageUrls.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        PhotoView photoView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            photoView = itemView.findViewById(R.id.photo_view_item);
        }
    }
}