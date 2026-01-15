package com.example.dalats.activity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.example.dalats.R;
import com.example.dalats.adapter.FullScreenImageAdapter; // Adapter mới ở bước 3
import java.util.ArrayList;

public class ImageDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_detail);

        // Nhận dữ liệu
        ArrayList<String> listUrl = getIntent().getStringArrayListExtra("LIST_URL");
        int position = getIntent().getIntExtra("POSITION", 0);

        ViewPager2 viewPager = findViewById(R.id.viewPagerFull);
        ImageView btnClose = findViewById(R.id.btn_close);
        TextView tvCounter = findViewById(R.id.tv_counter);

        if (listUrl != null && !listUrl.isEmpty()) {
            // Gắn Adapter FullScreen
            FullScreenImageAdapter adapter = new FullScreenImageAdapter(listUrl);
            viewPager.setAdapter(adapter);

            // Nhảy đến đúng vị trí ảnh vừa bấm
            viewPager.setCurrentItem(position, false);

            // Set số trang ban đầu (VD: 1/5)
            tvCounter.setText((position + 1) + "/" + listUrl.size());

            // Cập nhật số trang khi lướt
            viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    super.onPageSelected(position);
                    tvCounter.setText((position + 1) + "/" + listUrl.size());
                }
            });
        }

        btnClose.setOnClickListener(v -> finish());
    }
}