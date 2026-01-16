package com.example.dalats.model;

public class UpdateProfileRequest {
    private String fullName;
    private String email;

    public UpdateProfileRequest(String fullName, String email) {
        this.fullName = fullName;
        this.email = email;
    }
}

