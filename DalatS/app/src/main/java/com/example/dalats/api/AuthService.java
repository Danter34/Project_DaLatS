package com.example.dalats.api;

import com.example.dalats.model.AuthResponse;
import com.example.dalats.model.LoginRequest;
import com.example.dalats.model.RegisterRequest;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthService {

    @POST("api/Auth/login")
    Call<AuthResponse> login(@Body LoginRequest request);

    @POST("api/Auth/register")
    Call<ResponseBody> register(@Body RegisterRequest request);
}