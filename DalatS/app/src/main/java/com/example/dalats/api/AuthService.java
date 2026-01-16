package com.example.dalats.api;

import com.example.dalats.model.AuthResponse;
import com.example.dalats.model.ChangePasswordRequest;
import com.example.dalats.model.CheckResetCodeRequest;
import com.example.dalats.model.ForgotPasswordRequest;
import com.example.dalats.model.LoginRequest;
import com.example.dalats.model.RegisterRequest;
import com.example.dalats.model.ResetPasswordRequest;
import com.example.dalats.model.UpdateProfileRequest;
import com.example.dalats.model.UserDashboardResponse;
import com.example.dalats.model.UserProfileResponse;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;

public interface AuthService {

    @POST("api/Auth/login")
    Call<AuthResponse> login(@Body LoginRequest request);

    @POST("api/Auth/register")
    Call<ResponseBody> register(@Body RegisterRequest request);

    @GET("api/Auth/profile")
    Call<UserProfileResponse> getProfile();

    @PUT("api/Auth/update-profile")
    Call<ResponseBody> updateProfile(@Body UpdateProfileRequest request); // Trả về text thông báo

    @GET("api/Auth/dashboard")
    Call<UserDashboardResponse> getDashboard();

    @PUT("api/Auth/change-password")
    Call<Void> changePassword(@Body ChangePasswordRequest request);

    @POST("api/Auth/forgot-password")
    Call<ResponseBody> forgotPassword(@Body ForgotPasswordRequest request);

    @POST("api/Auth/check-reset-code")
    Call<ResponseBody> checkResetCode(@Body CheckResetCodeRequest request);

    @POST("api/Auth/reset-password")
    Call<ResponseBody> resetPassword(@Body ResetPasswordRequest request);
}