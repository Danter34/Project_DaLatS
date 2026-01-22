package com.example.dalats.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.dalats.R;
import com.example.dalats.api.ApiClient;
import com.example.dalats.data.DalatData;
import com.example.dalats.model.CreateIncidentDTO;
import com.example.dalats.model.Incident;
import com.example.dalats.model.IncidentCategory;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReportIncidentActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;
    private static final int PERMISSION_LOCATION_CODE = 100;
    private static final int PERMISSION_CAMERA_CODE = 101;

    private boolean isForceCreate = false;

    // View
    private Spinner spinnerCategory, spinnerWard, spinnerStreet;
    private EditText edtTitle, edtDesc, edtAddressDetail;
    private ImageView btnAddImage, btnGetLocation;
    private Button btnSubmit;
    private LinearLayout layoutImagesPreview;

    // Data
    private List<IncidentCategory> categoryList = new ArrayList<>();
    private List<File> selectedFiles = new ArrayList<>();

    private double currentLat = 0.0, currentLng = 0.0;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_incident);

        initView();
        setupSpinners();
        loadCategoriesFromApi();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        btnAddImage.setOnClickListener(v -> showImageOptionDialog());
        btnGetLocation.setOnClickListener(v -> getCurrentLocation());
        btnSubmit.setOnClickListener(v -> submitIncident());
        findViewById(R.id.btn_back_report).setOnClickListener(v -> finish());
    }

    private void initView() {
        spinnerCategory = findViewById(R.id.spinner_category);
        spinnerWard = findViewById(R.id.spinner_ward);
        spinnerStreet = findViewById(R.id.spinner_street);
        edtTitle = findViewById(R.id.edt_title);
        edtDesc = findViewById(R.id.edt_desc);
        edtAddressDetail = findViewById(R.id.edt_address_detail);
        btnAddImage = findViewById(R.id.btn_add_image);
        btnGetLocation = findViewById(R.id.btn_get_location);
        btnSubmit = findViewById(R.id.btn_submit);
        layoutImagesPreview = findViewById(R.id.layout_images_preview);
    }

    private void setupSpinners() {
        ArrayAdapter<String> wardAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, DalatData.WARDS);
        wardAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerWard.setAdapter(wardAdapter);

        ArrayAdapter<String> streetAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, DalatData.STREETS);
        streetAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStreet.setAdapter(streetAdapter);
    }

    private void loadCategoriesFromApi() {
        ApiClient.getIncidentService().getCategories().enqueue(new Callback<List<IncidentCategory>>() {
            @Override
            public void onResponse(@NonNull Call<List<IncidentCategory>> call, @NonNull Response<List<IncidentCategory>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    categoryList = response.body();
                    List<String> catNames = new ArrayList<>();
                    for (IncidentCategory c : categoryList) {
                        catNames.add(c.getName());
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(ReportIncidentActivity.this, android.R.layout.simple_spinner_item, catNames);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerCategory.setAdapter(adapter);
                }
            }
            @Override
            public void onFailure(@NonNull Call<List<IncidentCategory>> call, @NonNull Throwable t) {
                Toast.makeText(ReportIncidentActivity.this, "Lỗi tải danh mục", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --- 1. XỬ LÝ ẢNH ---
    private void showImageOptionDialog() {
        String[] options = {"Chụp ảnh", "Chọn từ thư viện"};
        new AlertDialog.Builder(this)
                .setTitle("Thêm ảnh minh họa")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) checkCameraPermissionAndOpen();
                    else openGallery();
                })
                .show();
    }

    private void checkCameraPermissionAndOpen() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_CAMERA_CODE);
        } else {
            openCamera();
        }
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } else {
            Toast.makeText(this, "Lỗi: Không tìm thấy ứng dụng Camera", Toast.LENGTH_SHORT).show();
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CAMERA_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Bạn cần cấp quyền Camera", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == PERMISSION_LOCATION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) return;

        try {
            if (requestCode == REQUEST_IMAGE_CAPTURE && data != null) {
                Bundle extras = data.getExtras();
                if (extras != null && extras.get("data") != null) {
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    File file = bitmapToFile(imageBitmap);
                    addFileToListAndPreview(file, imageBitmap);
                }
            } else if (requestCode == REQUEST_IMAGE_PICK && data != null) {
                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null) {
                    File file = uriToFile(selectedImageUri);
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                    addFileToListAndPreview(file, bitmap);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi xử lý ảnh", Toast.LENGTH_SHORT).show();
        }
    }

    private void addFileToListAndPreview(File file, Bitmap bitmap) {
        if (file == null || bitmap == null) return;
        selectedFiles.add(file);

        ImageView img = new ImageView(this);
        int size = (int) (100 * getResources().getDisplayMetrics().density);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
        params.setMargins(0, 0, 16, 0);
        img.setLayoutParams(params);
        img.setScaleType(ImageView.ScaleType.CENTER_CROP);
        img.setImageBitmap(bitmap);

        img.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Xóa ảnh?")
                    .setMessage("Bạn có muốn bỏ ảnh này không?")
                    .setPositiveButton("Xóa", (dialog, which) -> {
                        layoutImagesPreview.removeView(img);
                        selectedFiles.remove(file);
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });

        layoutImagesPreview.addView(img, 0);
    }

    // --- CÁC HÀM TIỆN ÍCH FILE ---
    private File bitmapToFile(Bitmap bitmap) throws IOException {
        File filesDir = getCacheDir();
        File imageFile = new File(filesDir, "cam_" + System.currentTimeMillis() + ".jpg");
        FileOutputStream fos = new FileOutputStream(imageFile);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        fos.flush();
        fos.close();
        return imageFile;
    }

    private File uriToFile(Uri uri) throws IOException {
        File file = null;
        String fileName = getFileName(uri);
        File cacheDir = getCacheDir();
        file = new File(cacheDir, fileName);
        InputStream inputStream = getContentResolver().openInputStream(uri);
        FileOutputStream outputStream = new FileOutputStream(file);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, length);
        }
        outputStream.close();
        inputStream.close();
        return file;
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index >= 0) result = cursor.getString(index);
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) result = result.substring(cut + 1);
        }
        return result;
    }

    // --- XỬ LÝ VỊ TRÍ ---
    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_LOCATION_CODE);
            return;
        }
        btnGetLocation.setEnabled(false); // Chống click nhiều lần
        Toast.makeText(this, "Đang lấy vị trí...", Toast.LENGTH_SHORT).show();

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            btnGetLocation.setEnabled(true);
            if (location != null) {
                currentLat = location.getLatitude();
                currentLng = location.getLongitude();
                autoFillAddress(location);
            } else {
                Toast.makeText(this, "Không tìm thấy vị trí. Hãy bật GPS và thử lại.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void autoFillAddress(Location location) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String street = address.getThoroughfare();
                String houseNum = address.getSubThoroughfare();
                String detailText = "";
                if (houseNum != null) detailText += houseNum + " ";
                if (street != null) detailText += street;

                if (detailText.trim().isEmpty()) {
                    String fullLine = address.getAddressLine(0);
                    if (fullLine.contains(",")) detailText = fullLine.substring(0, fullLine.indexOf(","));
                    else detailText = fullLine;
                }
                edtAddressDetail.setText(detailText);

                String fullAddress = address.getAddressLine(0);
                findAndSelectSpinner(spinnerWard, fullAddress, "Phường");
                String searchStreet = (street != null) ? street : fullAddress;
                findAndSelectSpinner(spinnerStreet, searchStreet, "");
                Toast.makeText(this, "Đã cập nhật vị trí!", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void findAndSelectSpinner(Spinner spinner, String targetText, String prefix) {
        if (targetText == null) return;
        SpinnerAdapter adapter = spinner.getAdapter();
        String normalizeTarget = targetText.toLowerCase().trim();
        for (int i = 0; i < adapter.getCount(); i++) {
            String item = adapter.getItem(i).toString();
            String normalizeItem = item.toLowerCase().trim();
            if (normalizeItem.contains(normalizeTarget) || normalizeTarget.contains(normalizeItem)) {
                if (prefix.equals("Phường")) {
                    String numItem = normalizeItem.replace("phường", "").trim();
                    if (normalizeTarget.contains("phường " + numItem) || normalizeTarget.contains("ward " + numItem)) {
                        spinner.setSelection(i);
                        return;
                    }
                } else {
                    spinner.setSelection(i);
                    return;
                }
            }
        }
    }



    private void submitIncident() {
        String title = edtTitle.getText().toString().trim();
        String desc = edtDesc.getText().toString().trim();
        String addr = edtAddressDetail.getText().toString().trim();

        // 1. Kiểm tra text
        if (title.isEmpty() || desc.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tiêu đề và mô tả", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. [QUAN TRỌNG] Kiểm tra bắt buộc phải có ảnh
        if (selectedFiles.isEmpty()) {
            Toast.makeText(this, "Vui lòng chụp hoặc chọn ít nhất 1 ảnh minh họa", Toast.LENGTH_SHORT).show();
            return;
        }

        // 3. Kiểm tra vị trí
        if (currentLat == 0.0 || currentLng == 0.0) {
            Toast.makeText(this, "Vui lòng lấy vị trí trước khi gửi", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSubmit.setEnabled(false);
        btnSubmit.setText("ĐANG GỬI...");

        String ward = "";
        if (spinnerWard.getSelectedItem() != null) ward = spinnerWard.getSelectedItem().toString();
        String street = "";
        if (spinnerStreet.getSelectedItem() != null) street = spinnerStreet.getSelectedItem().toString();

        int catId = 0;
        if (!categoryList.isEmpty()) {
            catId = categoryList.get(spinnerCategory.getSelectedItemPosition()).getCategoryId();
        } else {
            Toast.makeText(this, "Chưa tải xong danh mục", Toast.LENGTH_SHORT).show();
            btnSubmit.setEnabled(true);
            return;
        }

        CreateIncidentDTO dto = new CreateIncidentDTO(
                title, desc, addr, ward, street,
                currentLat, currentLng, catId,
                currentLat, currentLng,
                isForceCreate
        );

        ApiClient.getIncidentService().createIncident(dto).enqueue(new Callback<Incident>() {
            @Override
            public void onResponse(@NonNull Call<Incident> call, @NonNull Response<Incident> response) {
                // THÀNH CÔNG -> Chắc chắn có ảnh -> Upload luôn
                if (response.isSuccessful() && response.body() != null) {
                    int newId = response.body().getIncidentId();
                    uploadImages(newId); // Không cần check isEmpty() nữa
                }
                // LỖI (Trùng lặp / Khoảng cách)
                else if (response.code() == 409) {
                    handleConflictError(response);
                }
                // LỖI KHÁC
                else {
                    resetSubmitButton();
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Lỗi";
                        try {
                            JSONObject errJson = new JSONObject(errorBody);
                            String msg = errJson.optString("message", errorBody);
                            Toast.makeText(ReportIncidentActivity.this, msg, Toast.LENGTH_LONG).show();
                        } catch (Exception ignored) {
                            Toast.makeText(ReportIncidentActivity.this, "Lỗi: " + errorBody, Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(ReportIncidentActivity.this, "Lỗi server: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Incident> call, @NonNull Throwable t) {
                resetSubmitButton();
                Toast.makeText(ReportIncidentActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void resetSubmitButton() {
        btnSubmit.setEnabled(true);
        btnSubmit.setText("GỬI BÁO CÁO");
    }

    private void handleConflictError(Response<Incident> response) {
        resetSubmitButton();
        try {
            if (response.errorBody() == null) return;
            String errorJson = response.errorBody().string();
            JSONObject jsonObject = new JSONObject(errorJson);
            String type = jsonObject.optString("type");
            String message = jsonObject.optString("message");

            if ("vote".equals(type)) {
                showVoteSuggestionDialog(message);
            } else if ("distance".equals(type)) {
                showDistanceWarningDialog(message);
            } else {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi đọc phản hồi", Toast.LENGTH_SHORT).show();
        }
    }

    private void showVoteSuggestionDialog(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Phát hiện trùng lặp")
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Ủng hộ (Vote)", (dialog, which) -> {
                    Toast.makeText(this, "Cảm ơn bạn đã Vote +1!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton("Vẫn báo cáo", (dialog, which) -> {
                    isForceCreate = true;
                    submitIncident();
                })
                .setNeutralButton("Hủy bỏ", (dialog, which) -> finish())
                .show();
    }

    private void showDistanceWarningDialog(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Cảnh báo vị trí")
                .setMessage(message)
                .setCancelable(false)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Báo cáo nguội", (dialog, which) -> {
                    isForceCreate = true;
                    submitIncident();
                })
                .setNegativeButton("Kiểm tra lại", null)
                .show();
    }

    // --- UPLOAD ẢNH (Có xử lý Rollback nếu ảnh lỗi) ---
    private void uploadImages(int incidentId) {
        List<MultipartBody.Part> parts = new ArrayList<>();
        for (File file : selectedFiles) {
            RequestBody reqFile = RequestBody.create(MediaType.parse("image/*"), file);
            MultipartBody.Part part = MultipartBody.Part.createFormData("files", file.getName(), reqFile);
            parts.add(part);
        }

        ApiClient.getIncidentService().uploadImages(incidentId, parts).enqueue(new Callback<List<String>>() {
            @Override
            public void onResponse(@NonNull Call<List<String>> call, @NonNull Response<List<String>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ReportIncidentActivity.this, "Gửi báo cáo & ảnh thành công!", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    // BỊ AI CHẶN -> HIỆN POPUP VÀ ĐÓNG MÀN HÌNH (VÌ SỰ CỐ ĐÃ BỊ XÓA)
                    try {
                        String errorBody = response.errorBody().string();
                        JSONObject jsonObject = new JSONObject(errorBody);
                        String msg = jsonObject.optString("message", "Ảnh bị từ chối.");

                        new AlertDialog.Builder(ReportIncidentActivity.this)
                                .setTitle("Báo cáo thất bại")
                                .setMessage("Hệ thống phát hiện ảnh không phù hợp.\n\n" +
                                        "Lý do: " + msg + "\n\n" )
                                .setPositiveButton("Đóng", (dialog, which) -> {
                                    finish();
                                })
                                .setCancelable(false)
                                .show();

                    } catch (Exception e) {
                        Toast.makeText(ReportIncidentActivity.this, "Lỗi upload ảnh: " + response.code(), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<String>> call, @NonNull Throwable t) {
                Toast.makeText(ReportIncidentActivity.this, "Sự cố đã tạo nhưng lỗi mạng khi tải ảnh", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}