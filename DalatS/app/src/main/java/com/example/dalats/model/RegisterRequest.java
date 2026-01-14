package com.example.dalats.model;

public class RegisterRequest {
    private String fullName;
    private String email;
    private String password;

    public RegisterRequest(String fullName, String email, String password) {
        this.fullName = fullName;
        this.email = email;
        this.password = password;
    }
}
