package com.example.dalats.model;

public class CreateIncidentDTO {
    private String title;
    private String description;
    private String address;     // Địa chỉ chi tiết (số nhà...)
    private String ward;        // Phường
    private String streetName;  // Tên đường
    private double latitude;    // Ẩn với user nhưng vẫn phải gửi
    private double longitude;
    private int categoryId;

    public CreateIncidentDTO(String title, String description, String address, String ward, String streetName, double latitude, double longitude, int categoryId) {
        this.title = title;
        this.description = description;
        this.address = address;
        this.ward = ward;
        this.streetName = streetName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.categoryId = categoryId;
    }
}
