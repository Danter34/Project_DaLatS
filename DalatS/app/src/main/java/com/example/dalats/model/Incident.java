package com.example.dalats.model;

import java.util.List;

public class Incident {
    private int incidentId;
    private String title;
    private String description;
    private String ward;
    private String streetName;
    private double latitude;
    private double longitude;
    private String status;
    private int alertLevel; // 1: Green, 2: Orange, 3: Red
    private String createdAt;
    private String categoryName;
    private List<IncidentImage> images;

    // Getters
    public int getIncidentId() { return incidentId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getWard() { return ward; }
    public String getStreetName() { return streetName; }
    public String getStatus() { return status; }
    public int getAlertLevel() { return alertLevel; }
    public String getCreatedAt() { return createdAt; }
    public String getCategoryName() { return categoryName; }
    public List<IncidentImage> getImages() { return images; }
}
