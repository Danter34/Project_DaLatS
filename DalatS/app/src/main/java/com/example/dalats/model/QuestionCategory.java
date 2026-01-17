package com.example.dalats.model;
import com.google.gson.annotations.SerializedName;

public class QuestionCategory {
    @SerializedName("categoryId")
    private int categoryId;
    @SerializedName("name")
    private String name;

    // Getter
    public int getCategoryId() { return categoryId; }
    public String getName() { return name; }

    // Để hiển thị trên Spinner dễ dàng
    @Override
    public String toString() { return name; }
}