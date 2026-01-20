package com.example.dalats.model;
import com.google.gson.annotations.SerializedName;

public class UserDashboardResponse {
    // --- Các trường cũ ---
    @SerializedName("totalIncidents")
    private int total;
    @SerializedName("pendingIncidents")
    private int pending;
    @SerializedName("processingIncidents")
    private int processing;
    @SerializedName("completedIncidents")
    private int completed;
    @SerializedName("rejectedIncidents")
    private int rejected;

    // --- CÁC TRƯỜNG MỚI (Trust Score) ---
    @SerializedName("trustScore")
    private int trustScore;

    @SerializedName("dailyReportLimit")
    private int dailyReportLimit;

    @SerializedName("usedDailyQuota")
    private int usedDailyQuota;

    @SerializedName("trustStatus")
    private String trustStatus;

    // Getters cũ
    public int getTotal() { return total; }
    public int getPending() { return pending; }
    public int getProcessing() { return processing; }
    public int getCompleted() { return completed; }
    public int getRejected() { return rejected; }

    // Getters mới
    public int getTrustScore() { return trustScore; }
    public int getDailyReportLimit() { return dailyReportLimit; }
    public int getUsedDailyQuota() { return usedDailyQuota; }
    public String getTrustStatus() { return trustStatus; }
}