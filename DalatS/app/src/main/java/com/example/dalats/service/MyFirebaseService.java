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
import android.media.AudioAttributes;
import android.content.ContentResolver;
import android.net.Uri;
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
        // [QUAN TRỌNG] Đổi tên Channel ID mỗi khi thay đổi cấu hình âm thanh/rung
        // Nếu giữ nguyên ID cũ, Android sẽ nhớ cài đặt cũ và không cập nhật cái mới đâu.
        String channelId = "SafeDalat_Alert_Channel_V2";

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // 1. CẤU HÌNH ÂM THANH
        // Cách A: Dùng âm thanh mặc định của hệ thống (dễ nhất)
        // Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        // Cách B: Dùng âm thanh riêng (file alert_sound.mp3 trong thư mục res/raw)
        Uri soundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + getPackageName() + "/" + R.raw.arlet);

        // 2. CẤU HÌNH RUNG (Mẫu: nghỉ 0ms, rung 500ms, nghỉ 200ms, rung 500ms)
        long[] vibrationPattern = {0, 500, 200, 500};

        // 3. TẠO CHANNEL (Cho Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Cảnh báo SafeDalat",
                    NotificationManager.IMPORTANCE_HIGH // Phải là HIGH mới bung popup và kêu to
            );

            // Thiết lập Âm thanh cho Channel
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build();
            channel.setSound(soundUri, audioAttributes);

            // Thiết lập Rung cho Channel
            channel.enableVibration(true);
            channel.setVibrationPattern(vibrationPattern);

            notificationManager.createNotificationChannel(channel);
        }

        // 4. TẠO THÔNG BÁO
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                // Thiết lập cho các máy Android cũ (< 8.0)
                .setSound(soundUri)
                .setVibrate(vibrationPattern);

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