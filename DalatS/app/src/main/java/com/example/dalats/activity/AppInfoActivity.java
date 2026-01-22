package com.example.dalats.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.dalats.R;

public class AppInfoActivity extends AppCompatActivity {

    // Thay đổi các thông tin liên hệ của bạn tại đây
    private static final String FACEBOOK_URL = "https://www.facebook.com/Dalat.in.my.heart"; // Link Fanpage hoặc cá nhân
    private static final String ZALO_URL = "https://zalo.me/0912345678"; // Link Zalo (zalo.me/SĐT)
    private static final String EMAIL_ADDRESS = "hotro@safedalat.com"; // Email hỗ trợ

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_info);


        findViewById(R.id.btn_back_info).setOnClickListener(v -> finish());


        findViewById(R.id.btn_contact_fb).setOnClickListener(v -> openWebUrl(FACEBOOK_URL));


        findViewById(R.id.btn_contact_zalo).setOnClickListener(v -> openWebUrl(ZALO_URL));


        findViewById(R.id.btn_contact_email).setOnClickListener(v -> sendEmail());


        findViewById(R.id.btn_privacy_policy).setOnClickListener(v -> showPrivacyDialog());
    }

    // Hàm mở trình duyệt web (Dùng chung cho FB và Zalo)
    private void openWebUrl(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Không tìm thấy ứng dụng để mở liên kết", Toast.LENGTH_SHORT).show();
        }
    }

    // Hàm mở ứng dụng Email
    private void sendEmail() {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:" + EMAIL_ADDRESS)); // Chỉ mở các ứng dụng xử lý mail
        intent.putExtra(Intent.EXTRA_SUBJECT, "Góp ý về ứng dụng Safe Dalat"); // Tiêu đề mail mặc định

        try {
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Không tìm thấy ứng dụng Email trên máy", Toast.LENGTH_SHORT).show();
        }
    }

    // Hàm hiện Popup Chính sách bảo mật
    private void showPrivacyDialog() {
        String privacyContent = "1. Thu thập dữ liệu: Ứng dụng thu thập tên, email để tạo tài khoản.\n\n" +
                "2. Vị trí: Ứng dụng sử dụng vị trí để báo cáo sự cố chính xác.\n\n" +
                "3. Bảo mật: Chúng tôi cam kết không chia sẻ dữ liệu cá nhân cho bên thứ ba trái phép.\n\n" +
                "4. Quyền của bạn: Bạn có quyền yêu cầu xóa tài khoản và dữ liệu bất cứ lúc nào.";

        new AlertDialog.Builder(this)
                .setTitle("Chính sách bảo mật & Pháp lý")
                .setMessage(privacyContent)
                .setPositiveButton("Đóng", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }
}