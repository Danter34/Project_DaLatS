package com.example.dalats.model;

public class ChangePasswordRequest {
    private String oldPassword;
    private String newPassword;
    public ChangePasswordRequest(String old, String _new) {
        this.oldPassword = old;
        this.newPassword = _new;
    }
}
