package com.example.dalats.model;

import com.google.gson.annotations.SerializedName;

public class IncidentCategory {
    @SerializedName("categoryId")
    private int categoryId;

    @SerializedName("name")
    private String name;

    // Constructor
    public IncidentCategory(int categoryId, String name) {
        this.categoryId = categoryId;
        this.name = name;
    }

    // Getters (Quan trọng: Phải có để code gọi .getName() không bị lỗi)
    public int getCategoryId() {
        return categoryId;
    }

    public String getName() {
        return name;
    }
}