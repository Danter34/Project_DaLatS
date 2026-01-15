package com.example.dalats.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Incident {
    @SerializedName("incidentId")
    private int incidentId;
    @SerializedName("title")
    private String title;
    @SerializedName("description")
    private String description;
    @SerializedName("ward")
    private String ward;
    @SerializedName("streetName")
    private String streetName;
    @SerializedName("status")
    private String status;
    @SerializedName("alertLevel")
    private int alertLevel; // 1: Green, 2: Orange, 3: Red
    @SerializedName("createdAt")
    private String createdAt;
    @SerializedName("categoryName")
    private String categoryName;
    @SerializedName("images")
    private List<IncidentImage> images;

    // Getters
    public int getIncidentId() { return incidentId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getStatus() { return status; }
    public int getAlertLevel() { return alertLevel; }
    public String getCreatedAt() { return createdAt; }
    public String getCategoryName() { return categoryName; }
    public List<IncidentImage> getImages() { return images; }

    // Hàm tiện ích tự viết thêm để ghép địa chỉ hiển thị
    public String getFullAddress() {
        return streetName + ", " + ward;
    }
}