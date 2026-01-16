package com.example.dalats.model;
public class CheckResetCodeRequest {
    private String email;
    private String code;
    public CheckResetCodeRequest(String email, String code) {
        this.email = email;
        this.code = code;
    }
}