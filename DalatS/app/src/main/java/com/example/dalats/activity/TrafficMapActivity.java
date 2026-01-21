package com.example.dalats.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.dalats.R;
import com.example.dalats.api.ApiClient;
import com.example.dalats.model.TrafficHotspot;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TrafficMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private SwitchMaterial switchTraffic;
    private TextView tvHotspotCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_traffic_map);

        // Khởi tạo Views
        switchTraffic = findViewById(R.id.switch_traffic_layer);
        tvHotspotCount = findViewById(R.id.tv_hotspot_count);
        ImageButton btnBack = findViewById(R.id.btn_back);

        btnBack.setOnClickListener(v -> finish());

        // Load Map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_fragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Sự kiện bật tắt Layer Google
        switchTraffic.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (mMap != null) {
                mMap.setTrafficEnabled(isChecked);
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // 1. Cấu hình mặc định: Đà Lạt
        LatLng dalat = new LatLng(11.940419, 108.458313);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(dalat, 13));

        // 2. Bật lớp Giao thông của Google (Xanh/Đỏ/Vàng)
        mMap.setTrafficEnabled(true);

        // 3. Gọi API để lấy điểm nóng từ SafeDalat
        loadSafeDalatHotspots();
    }

    private void loadSafeDalatHotspots() {
        ApiClient.getIncidentService().getTrafficHotspots().enqueue(new Callback<List<TrafficHotspot>>() {
            @Override
            public void onResponse(Call<List<TrafficHotspot>> call, Response<List<TrafficHotspot>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<TrafficHotspot> hotspots = response.body();
                    drawHotspotsOnMap(hotspots);

                    if (hotspots.isEmpty()) {
                        tvHotspotCount.setText("Hiện tại không có điểm nóng nào từ cộng đồng.");
                        tvHotspotCount.setTextColor(Color.parseColor("#4CAF50")); // Màu xanh
                    } else {
                        tvHotspotCount.setText("Phát hiện " + hotspots.size() + " điểm ùn tắc/nguy hiểm từ cộng đồng.");
                        tvHotspotCount.setTextColor(Color.RED);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<TrafficHotspot>> call, Throwable t) {
                Toast.makeText(TrafficMapActivity.this, "Lỗi tải dữ liệu giao thông", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void drawHotspotsOnMap(List<TrafficHotspot> hotspots) {
        for (TrafficHotspot spot : hotspots) {
            LatLng location = new LatLng(spot.getLatitude(), spot.getLongitude());

            // A. Vẽ vòng tròn đỏ cảnh báo phạm vi 150m
            mMap.addCircle(new CircleOptions()
                    .center(location)
                    .radius(150) // Bán kính 150 mét
                    .strokeColor(Color.RED)
                    .fillColor(0x22FF0000) // Màu đỏ mờ (Transparent Red)
                    .strokeWidth(2));

            // B. Thêm Marker (Icon cảnh báo)
            mMap.addMarker(new MarkerOptions()
                    .position(location)
                    .title(spot.getStreetName())
                    .snippet(spot.getAlertMessage())
                    // Bạn có thể thay icon bằng hình tam giác cảnh báo
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
        }
    }
}