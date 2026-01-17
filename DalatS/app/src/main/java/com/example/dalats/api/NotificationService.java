package com.example.dalats.api;

import com.example.dalats.model.NotificationDTO;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface NotificationService {
    @GET("api/Notifications/get")
    Call<List<NotificationDTO>> getMyNotifications();

    @PUT("api/Notifications/read/{id}")
    Call<Void> markAsRead(@Path("id") int id);
}