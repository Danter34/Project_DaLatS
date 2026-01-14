package com.example.dalats.api;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static final String BASE_URL = "http://10.0.2.2:5084/";

    private static Retrofit retrofit = null;
    private static String authToken = null; // Biến lưu Token

    // Hàm cập nhật Token sau khi Login
    public static void setAuthToken(String token) {
        authToken = token;
        retrofit = null; // Reset để khởi tạo lại với token mới
    }

    private static Retrofit getClient() {
        if (retrofit == null) {
            OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();

            // Thêm Interceptor để tự động kẹp Token vào Header
            clientBuilder.addInterceptor(chain -> {
                Request original = chain.request();
                Request.Builder builder = original.newBuilder();

                if (authToken != null && !authToken.isEmpty()) {
                    builder.header("Authorization", "Bearer " + authToken);
                }

                return chain.proceed(builder.build());
            });

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(clientBuilder.build())
                    .build();
        }
        return retrofit;
    }

    // --- Services ---
    public static AuthService getAuthService() {
        return getClient().create(AuthService.class);
    }

    public static IncidentService getIncidentService() {
        return getClient().create(IncidentService.class);
    }

    public static EnviService getEnviService() {
        return getClient().create(EnviService.class);
    }
}