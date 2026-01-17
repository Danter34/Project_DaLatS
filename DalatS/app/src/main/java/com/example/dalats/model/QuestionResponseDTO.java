package com.example.dalats.model;
import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class QuestionResponseDTO implements Serializable {
    @SerializedName("questionId")
    private int questionId;
    @SerializedName("content")
    private String content;
    @SerializedName("userName")
    private String userName;
    @SerializedName("questionCategoryName")
    private String questionCategoryName;
    @SerializedName("createdAt")
    private String createdAt;
    @SerializedName("answers")
    private List<AnswerResponseDTO> answers;

    public int getQuestionId() { return questionId; }
    public String getContent() { return content; }
    public String getUserName() { return userName; }
    public String getQuestionCategoryName() { return questionCategoryName; }
    public String getCreatedAt() { return createdAt; }
    public List<AnswerResponseDTO> getAnswers() { return answers; }
}