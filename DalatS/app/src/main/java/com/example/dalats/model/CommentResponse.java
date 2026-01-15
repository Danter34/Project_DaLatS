package com.example.dalats.model;
import com.google.gson.annotations.SerializedName;

public class CommentResponse {
    @SerializedName("commentId")
    private int commentId;
    @SerializedName("content")
    private String content;
    @SerializedName("createdAt")
    private String createdAt;
    @SerializedName("fullName")
    private String fullName;
    @SerializedName("role")
    private String role;

    public String getContent() { return content; }
    public String getFullName() { return fullName; }
    public String getCreatedAt() { return createdAt; }
    public String getRole() { return role; }
}