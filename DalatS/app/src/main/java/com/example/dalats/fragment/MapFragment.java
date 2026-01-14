package com.example.dalats.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.dalats.R;
import com.example.dalats.api.ApiClient;
import com.example.dalats.model.IncidentMap;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;

    // --- CẤU HÌNH GIỚI HẠN VÙNG ĐÀ LẠT ---
    // Tọa độ Tây Nam (Chân đèo Prenn/Mimosa)
    private static final LatLng DALAT_SW = new LatLng(11.850000, 108.350000);
    // Tọa độ Đông Bắc (Hướng đi Lạc Dương/Trại Mát)
    private static final LatLng DALAT_NE = new LatLng(12.020000, 108.550000);

    // Tạo Bounds từ 2 điểm trên
    private static final LatLngBounds DALAT_BOUNDS = new LatLngBounds(DALAT_SW, DALAT_NE);

    // Trung tâm Đà Lạt (Hồ Xuân Hương/Chợ)
    private static final LatLng DALAT_CENTER = new LatLng(11.940419, 108.437261);

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        // Khởi tạo Map Fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        return view;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // 1. Cài đặt giới hạn Camera (Chỉ cho phép xem ở Đà Lạt)
        mMap.setLatLngBoundsForCameraTarget(DALAT_BOUNDS);

        // 2. Cài đặt Zoom (Không cho zoom quá xa ra khỏi thành phố)
        mMap.setMinZoomPreference(12.0f); // Zoom nhỏ nhất (nhìn toàn cảnh TP)
        mMap.setMaxZoomPreference(18.0f); // Zoom lớn nhất (nhìn chi tiết đường)

        // 3. Di chuyển camera về trung tâm Đà Lạt
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(DALAT_CENTER, 13.0f));

        // 4. Gọi API lấy dữ liệu điểm
        loadMapData();
    }

    private void loadMapData() {
        ApiClient.getIncidentService().getMapData().enqueue(new Callback<List<IncidentMap>>() {
            @Override
            public void onResponse(Call<List<IncidentMap>> call, Response<List<IncidentMap>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<IncidentMap> incidents = response.body();

                    // Xóa marker cũ nếu có
                    mMap.clear();

                    for (IncidentMap item : incidents) {
                        addMarker(item);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<IncidentMap>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi tải bản đồ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addMarker(IncidentMap item) {
        LatLng position = new LatLng(item.getLatitude(), item.getLongitude());

        // Chọn màu marker dựa theo AlertLevel (Giống logic Adapter)
        float markerColor;
        switch (item.getAlertLevel()) {
            case 1: // Green
                markerColor = BitmapDescriptorFactory.HUE_YELLOW;
                break;
            case 2: // Orange
                markerColor = BitmapDescriptorFactory.HUE_ORANGE;
                break;
            case 3: // Red
                markerColor = BitmapDescriptorFactory.HUE_RED;
                break;
            default:
                markerColor = BitmapDescriptorFactory.HUE_AZURE;
        }

        mMap.addMarker(new MarkerOptions()
                .position(position)
                .title(item.getTitle()) // Tiêu đề khi click vào marker
                .snippet(item.getCategoryName() + " - " + item.getStatus()) // Mô tả ngắn
                .icon(BitmapDescriptorFactory.defaultMarker(markerColor))); // Đổi màu
    }
}