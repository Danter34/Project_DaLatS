package com.example.dalats.model;
import com.google.gson.annotations.SerializedName;

public class UserDashboardResponse {
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

    // Getters...
    public int getTotal() { return total; }
    public int getPending() { return pending; }
    public int getProcessing() { return processing; }
    public int getCompleted() { return completed; }
    public int getRejected() { return rejected; }
}