package com.example.dalats.model;
import com.google.gson.annotations.SerializedName;

public class NotificationDTO {
    @SerializedName("notificationId")
    private int notificationId;
    @SerializedName("message")
    private String message;
    @SerializedName("isRead")
    private boolean isRead;
    @SerializedName("createdAt")
    private String createdAt;

    // Getters & Setters
    public int getNotificationId() { return notificationId; }
    public String getMessage() { return message; }
    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
    public String getCreatedAt() { return createdAt; }
}