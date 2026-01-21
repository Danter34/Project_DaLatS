package com.example.dalats.model;

public class TrafficHotspot {
    private String streetName;
    private double latitude;
    private double longitude;
    private int reportCount;
    private String alertMessage;

    // Getters & Setters tự sinh
    public String getStreetName() { return streetName; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public int getReportCount() { return reportCount; }
    public String getAlertMessage() { return alertMessage; }
}