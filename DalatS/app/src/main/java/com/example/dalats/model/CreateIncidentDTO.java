package com.example.dalats.model;

public class CreateIncidentDTO {
    private String title;
    private String description;
    private String address;
    private String ward;
    private String streetName;
    private double latitude;    // Tọa độ sự cố (chọn trên map/nơi ghim)
    private double longitude;
    private int categoryId;

    // --- THÊM MỚI ---
    private double deviceLatitude;  // Tọa độ GPS thực tế của điện thoại
    private double deviceLongitude;
    private boolean isForceCreate;  // Cờ xác nhận (true = gửi bất chấp)

    // Cập nhật Constructor
    public CreateIncidentDTO(String title, String description, String address, String ward, String streetName,
                             double latitude, double longitude, int categoryId,
                             double deviceLatitude, double deviceLongitude, boolean isForceCreate) {
        this.title = title;
        this.description = description;
        this.address = address;
        this.ward = ward;
        this.streetName = streetName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.categoryId = categoryId;

        // Gán giá trị mới
        this.deviceLatitude = deviceLatitude;
        this.deviceLongitude = deviceLongitude;
        this.isForceCreate = isForceCreate;
    }
}