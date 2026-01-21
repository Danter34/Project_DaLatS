package com.example.dalats.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.dalats.R;
import com.example.dalats.activity.MainActivity;
import com.example.dalats.api.ApiClient;
import com.example.dalats.api.ApiService;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyFirebaseService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseService";

    // 1. Hàm này chạy khi có tin nhắn tới (App đang mở hoặc chạy ngầm)
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        // A. Xử lý thông báo (Notification Payload) - Thường dùng khi App đang chạy foreground
        if (remoteMessage.getNotification() != null) {
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();
            showNotification(title, body);
        }
        // B. Xử lý dữ liệu ngầm (Data Payload) - Backend gửi kèm data tùy chỉnh
        else if (remoteMessage.getData().size() > 0) {
            String title = remoteMessage.getData().get("title");
            String body = remoteMessage.getData().get("body");

            // Nếu backend không gửi title/body trong data thì dùng mặc định
            if (title == null) title = "Thông báo mới";
            if (body == null) body = "Bạn có tin nhắn mới từ hệ thống.";

            showNotification(title, body);
        }
    }

    // 2. Hàm này chạy khi Google cấp lại Token mới (khi cài lại app hoặc token hết hạn)
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "Refreshed token: " + token);

        // Gửi token mới lên Server ngay lập tức để đồng bộ
        sendRegistrationToServer(token);
    }

    // 3. Hàm hiển thị thông báo ra thanh trạng thái (Ting Ting)
    private void showNotification(String title, String body) {
        String channelId = "SafeDalat_Alert_Channel";

        // Intent: Khi bấm vào thông báo sẽ mở MainActivity
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        // Âm thanh mặc định của hệ thống
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Android 8.0 (Oreo) trở lên bắt buộc phải tạo Channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Thông báo SafeDalat", // Tên hiển thị trong cài đặt
                    NotificationManager.IMPORTANCE_HIGH // Mức độ cao để hiện popup
            );
            channel.setDescription("Nhận cảnh báo giao thông và tin tức");
            channel.enableLights(true);
            channel.enableVibration(true);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.mipmap.ic_launcher) // Tốt nhất nên dùng icon trong suốt (white icon) ở drawable
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true) // Bấm vào tự biến mất
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH) // Ưu tiên cao
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        // Hiển thị thông báo với ID ngẫu nhiên để không bị đè lên nhau
        int notificationId = new Random().nextInt();
        notificationManager.notify(notificationId, builder.build());
    }

    // 4. Gọi API Backend để cập nhật Token
    private void sendRegistrationToServer(String token) {
        // Kiểm tra xem user đã đăng nhập chưa bằng SharedPreferences
        SharedPreferences pref = getSharedPreferences("UserSession", MODE_PRIVATE);
        String savedToken = pref.getString("TOKEN", null);

        // Chỉ gửi lên Server nếu user ĐÃ ĐĂNG NHẬP (có JWT Token)
        if (savedToken != null) {
            // Cấu hình Auth cho Retrofit
            ApiClient.setAuthToken(savedToken);

            ApiService api = ApiClient.getClient().create(ApiService.class);

            // Gọi API Update FCM
            // Lưu ý: Gửi chuỗi token bọc trong dấu ngoặc kép để khớp với định dạng JSON string ở Backend
            api.updateFcmToken("\"" + token + "\"").enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        Log.d(TAG, "Đã đồng bộ FCM Token mới lên Server");
                    } else {
                        Log.e(TAG, "Lỗi đồng bộ Token: " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Log.e(TAG, "Lỗi mạng khi đồng bộ Token: " + t.getMessage());
                }
            });
        } else {
            Log.d(TAG, "User chưa đăng nhập, Token sẽ được gửi sau khi Login.");
            // Nếu chưa đăng nhập, MainActivity sẽ lo việc gửi token sau khi user login thành công.
        }
    }
}