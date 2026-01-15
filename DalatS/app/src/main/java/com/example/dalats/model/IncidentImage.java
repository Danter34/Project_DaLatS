package com.example.dalats.model;

import com.google.gson.annotations.SerializedName;

public class IncidentImage {
    @SerializedName("imageId")
    private int imageId;

    @SerializedName("filePath")
    private String filePath;

    public int getImageId() { return imageId; }
    public String getFilePath() { return filePath; }
}