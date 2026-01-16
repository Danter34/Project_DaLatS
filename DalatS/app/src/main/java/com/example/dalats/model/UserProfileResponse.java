package com.example.dalats.model;
import com.google.gson.annotations.SerializedName;

public class UserProfileResponse {
    @SerializedName("fullName")
    private String fullName;
    @SerializedName("email")
    private String email;
    @SerializedName("createdAt")
    private String createdAt;

    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getCreatedAt() { return createdAt; }
}
