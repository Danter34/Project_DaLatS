package com.example.dalats.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static final String BASE_URL = "http://10.0.2.2:5084/";

    private static Retrofit retrofit = null;

    private static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    // Hàm lấy AuthService
    public static AuthService getAuthService() {
        return getClient().create(AuthService.class);
    }

    // Hàm lấy IncidentService
    public static IncidentService getIncidentService() {
        return getClient().create(IncidentService.class);
    }
    public static EnviService getEnviService() {
        return getClient().create(EnviService.class);
    }
}