package com.example.dalats.model;
import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class AnswerResponseDTO implements Serializable {
    @SerializedName("answerId")
    private int answerId;
    @SerializedName("content")
    private String content;
    @SerializedName("responderName")
    private String responderName;
    @SerializedName("departmentName")
    private String departmentName;
    @SerializedName("createdAt")
    private String createdAt;

    public String getContent() { return content; }
    public String getResponderName() { return responderName; }
    public String getDepartmentName() { return departmentName; }
    public String getCreatedAt() { return createdAt; }
}