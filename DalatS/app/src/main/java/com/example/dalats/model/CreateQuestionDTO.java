package com.example.dalats.model;

public class CreateQuestionDTO {
    private String content;
    private int questionCategoryId;

    public CreateQuestionDTO(String content, int questionCategoryId) {
        this.content = content;
        this.questionCategoryId = questionCategoryId;
    }
}