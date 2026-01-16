package com.example.dalats.model;
public class ResetPasswordRequest {
    private String email;
    private String code;
    private String newPassword;
    public ResetPasswordRequest(String email, String code, String newPassword) {
        this.email = email;
        this.code = code;
        this.newPassword = newPassword;
    }
}