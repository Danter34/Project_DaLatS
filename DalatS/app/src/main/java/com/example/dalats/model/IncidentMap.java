package com.example.dalats.model;

import com.google.gson.annotations.SerializedName;

public class IncidentMap {

    @SerializedName("incidentId")
    private int incidentId;

    @SerializedName("title")
    private String title;

    @SerializedName("latitude")
    private double latitude;

    @SerializedName("longitude")
    private double longitude;

    @SerializedName("status")
    private String status;

    @SerializedName("alertLevel")
    private int alertLevel;

    @SerializedName("categoryName")
    private String categoryName;

    public int getIncidentId() { return incidentId; }
    public String getTitle() { return title; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getStatus() { return status; }
    public int getAlertLevel() { return alertLevel; }
    public String getCategoryName() { return categoryName; }
}
