package com.example.dalats.api;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import com.example.dalats.activity.MyApplication; // Import class vừa tạo
import com.example.dalats.activity.LoginActivity;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    // Đổi IP nếu chạy máy thật
    private static final String BASE_URL = "http://10.0.2.2:5084/";
    private static Retrofit retrofit;
    private static String authToken = null;

    public static void setAuthToken(String token) {
        authToken = token;
    }

    public static Retrofit getClient() {
        if (retrofit == null) {
            // Cấu hình OkHttp với Interceptor
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(chain -> {
                        Request original = chain.request();
                        Request.Builder builder = original.newBuilder();

                        // 1. Gắn Token vào Header
                        if (authToken != null) {
                            builder.header("Authorization", "Bearer " + authToken);
                        }

                        Request request = builder.build();
                        Response response = chain.proceed(request);

                        // 2. TỰ ĐỘNG ĐĂNG XUẤT NẾU BỊ KHÓA (LỖI 401)
                        if (response.code() == 401) {
                            handleGlobalLogout();
                        }

                        return response;
                    })
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    // Hàm đăng xuất toàn cục
    private static void handleGlobalLogout() {
        Context context = MyApplication.getInstance();
        if (context != null) {
            // Xóa dữ liệu đăng nhập
            SharedPreferences pref = context.getSharedPreferences("UserSession", Context.MODE_PRIVATE);
            pref.edit().clear().apply();
            authToken = null;

            // Chuyển về màn hình Login và xóa hết Activity cũ
            Intent intent = new Intent(context, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
        }
    }

    // --- Services ---
    public static AuthService getAuthService() {
        return getClient().create(AuthService.class);
    }

    public static IncidentService getIncidentService() {
        return getClient().create(IncidentService.class);
    }
    public static QAService getQAService() {
        return getClient().create(QAService.class);
    }
    public static NotificationService getNotificationService() {
        return getClient().create(NotificationService.class);
    }
    public static EnviService getEnviService() {
        return getClient().create(EnviService.class);
    }
}