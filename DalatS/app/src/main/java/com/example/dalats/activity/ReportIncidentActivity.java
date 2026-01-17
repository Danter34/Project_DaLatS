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

    // View
    private Spinner spinnerCategory, spinnerWard, spinnerStreet;
    private EditText edtTitle, edtDesc, edtAddressDetail;
    private ImageView btnAddImage, btnGetLocation;
    private Button btnSubmit;
    private LinearLayout layoutImagesPreview;

    // Data
    private List<IncidentCategory> categoryList = new ArrayList<>();
    private List<File> selectedFiles = new ArrayList<>(); // Danh sách file ảnh upload

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

        // Sự kiện
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

    // --- 1. XỬ LÝ CAMERA VÀ THƯ VIỆN ---
    private void showImageOptionDialog() {
        String[] options = {"Chụp ảnh", "Chọn từ thư viện"};
        new AlertDialog.Builder(this)
                .setTitle("Thêm ảnh sự cố")
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

    // --- 2. NHẬN KẾT QUẢ ẢNH VÀ HIỂN THỊ ---
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK) return; // Nếu user hủy hoặc back

        if (data == null && requestCode != REQUEST_IMAGE_CAPTURE) {
            Toast.makeText(this, "Lỗi: Dữ liệu ảnh trống", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Trường hợp 1: Chụp Camera
            if (requestCode == REQUEST_IMAGE_CAPTURE && data != null) {
                Bundle extras = data.getExtras();
                if (extras != null && extras.get("data") != null) {
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    File file = bitmapToFile(imageBitmap); // Chuyển Bitmap thành File
                    addFileToListAndPreview(file, imageBitmap);
                } else {
                    Toast.makeText(this, "Không lấy được ảnh từ Camera (Data null)", Toast.LENGTH_SHORT).show();
                }
            }
            // Trường hợp 2: Chọn từ Thư viện
            else if (requestCode == REQUEST_IMAGE_PICK && data != null) {
                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null) {
                    File file = uriToFile(selectedImageUri); // Chuyển Uri thành File
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                    addFileToListAndPreview(file, bitmap);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi xử lý ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // --- 3. HÀM HIỂN THỊ ẢNH VÀ XÓA ẢNH ---
    private void addFileToListAndPreview(File file, Bitmap bitmap) {
        if (file == null || bitmap == null) {
            Toast.makeText(this, "Lỗi: Không thể tạo file ảnh", Toast.LENGTH_SHORT).show();
            return;
        }

        selectedFiles.add(file); // Thêm vào list chờ upload

        // Tạo ImageView
        ImageView img = new ImageView(this);

        // Chuyển 100dp sang pixel
        int size = (int) (100 * getResources().getDisplayMetrics().density);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
        params.setMargins(0, 0, 16, 0); // Cách lề phải 16px

        img.setLayoutParams(params);
        img.setScaleType(ImageView.ScaleType.CENTER_CROP);
        img.setImageBitmap(bitmap);

        // --- TÍNH NĂNG XÓA ẢNH KHI CLICK ---
        img.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Xóa ảnh?")
                    .setMessage("Bạn có muốn bỏ ảnh này không?")
                    .setPositiveButton("Xóa", (dialog, which) -> {
                        // 1. Xóa khỏi giao diện
                        layoutImagesPreview.removeView(img);
                        // 2. Xóa khỏi danh sách file upload
                        selectedFiles.remove(file);
                        Toast.makeText(this, "Đã xóa ảnh", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });

        // Thêm ảnh vào ĐẦU danh sách (vị trí 0) để đẩy nút cộng ra sau
        // Layout: [Ảnh 2] [Ảnh 1] [Nút Cộng]
        layoutImagesPreview.addView(img, 0);

        Toast.makeText(this, "Chạm vào ảnh để xóa", Toast.LENGTH_SHORT).show();
    }

    // --- CÁC HÀM TIỆN ÍCH FILE ---
    private File bitmapToFile(Bitmap bitmap) {
        try {
            File filesDir = getCacheDir();
            File imageFile = new File(filesDir, "cam_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream fos = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
            return imageFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private File uriToFile(Uri uri) {
        File file = null;
        try {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
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
            // Lấy 1 kết quả địa chỉ tốt nhất
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);

                // 1. Xử lý hiển thị chi tiết (Số nhà + Tên đường)
                String street = address.getThoroughfare();      // Tên đường (VD: Bùi Thị Xuân)
                String houseNum = address.getSubThoroughfare(); // Số nhà (VD: 12)

                String detailText = "";
                if (houseNum != null) detailText += houseNum + " ";
                if (street != null) detailText += street;

                // Nếu Google không trả về tách biệt, lấy dòng địa chỉ đầu tiên
                if (detailText.trim().isEmpty()) {
                    String fullLine = address.getAddressLine(0);
                    // Cắt chuỗi trước dấu phẩy đầu tiên (thường là số nhà + đường)
                    if (fullLine.contains(",")) {
                        detailText = fullLine.substring(0, fullLine.indexOf(","));
                    } else {
                        detailText = fullLine;
                    }
                }
                edtAddressDetail.setText(detailText);

                // 2. Xử lý tự động chọn Spinner (Dùng Full Address để tìm kiếm cho chính xác)
                String fullAddress = address.getAddressLine(0); // VD: 12 Bùi Thị Xuân, Phường 2, TP Đà Lạt...

                // Tìm Phường trong Full Address
                findAndSelectSpinner(spinnerWard, fullAddress, "Phường");

                // Tìm Đường trong Full Address (hoặc dùng tên đường lấy được ở trên)
                String searchStreet = (street != null) ? street : fullAddress;
                findAndSelectSpinner(spinnerStreet, searchStreet, "");

                Toast.makeText(this, "Đã cập nhật vị trí!", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi dịch địa chỉ", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Hàm thông minh để tìm và chọn item trong Spinner
     * @param spinner Spinner cần chọn
     * @param targetText Chuỗi địa chỉ từ Google (VD: "Phường 2" hoặc "Bùi Thị Xuân")
     * @param prefix Tiền tố cần check (VD: "Phường") để tăng độ chính xác
     */
    private void findAndSelectSpinner(Spinner spinner, String targetText, String prefix) {
        if (targetText == null) return;

        SpinnerAdapter adapter = spinner.getAdapter();
        String normalizeTarget = targetText.toLowerCase().trim();

        for (int i = 0; i < adapter.getCount(); i++) {
            String item = adapter.getItem(i).toString();
            String normalizeItem = item.toLowerCase().trim();

            // Logic so sánh:
            // 1. Item chứa Target (VD: Item="Đường Bùi Thị Xuân", Target="Bùi Thị Xuân")
            // 2. Target chứa Item (VD: Target="12 Bùi Thị Xuân, P2", Item="Bùi Thị Xuân")

            if (normalizeItem.contains(normalizeTarget) || normalizeTarget.contains(normalizeItem)) {

                // Nếu là Phường, cần check kỹ hơn để tránh "Phường 1" khớp với "Phường 10"
                if (prefix.equals("Phường")) {
                    // Lấy số cuối của item (VD: "Phường 2" -> "2")
                    String numItem = normalizeItem.replace("phường", "").trim();
                    // Kiểm tra xem trong chuỗi target có chứa số đó riêng biệt không
                    // VD: "Phường 2," chứa "2" nhưng "Phường 12" cũng chứa "1", "2"
                    if (normalizeTarget.contains("phường " + numItem) || normalizeTarget.contains("ward " + numItem)) {
                        spinner.setSelection(i);
                        return;
                    }
                } else {
                    // Với tên đường thì đơn giản hơn
                    spinner.setSelection(i);
                    return;
                }
            }
        }
    }

    // --- GỬI API ---
    private void submitIncident() {
        String title = edtTitle.getText().toString().trim();
        String desc = edtDesc.getText().toString().trim();
        String addr = edtAddressDetail.getText().toString().trim();

        if (title.isEmpty() || desc.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tiêu đề và mô tả", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(this, "Chưa tải xong danh mục, vui lòng thử lại", Toast.LENGTH_SHORT).show();
            btnSubmit.setEnabled(true);
            return;
        }

        CreateIncidentDTO dto = new CreateIncidentDTO(title, desc, addr, ward, street, currentLat, currentLng, catId);

        ApiClient.getIncidentService().createIncident(dto).enqueue(new Callback<Incident>() {
            @Override
            public void onResponse(@NonNull Call<Incident> call, @NonNull Response<Incident> response) {
                if (response.isSuccessful() && response.body() != null) {
                    int newId = response.body().getIncidentId();
                    if (!selectedFiles.isEmpty()) {
                        uploadImages(newId);
                    } else {
                        Toast.makeText(ReportIncidentActivity.this, "Gửi thành công!", Toast.LENGTH_LONG).show();
                        finish();
                    }
                } else {
                    btnSubmit.setEnabled(true);
                    btnSubmit.setText("GỬI BÁO CÁO");
                    Toast.makeText(ReportIncidentActivity.this, "Lỗi tạo sự cố: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(@NonNull Call<Incident> call, @NonNull Throwable t) {
                btnSubmit.setEnabled(true);
                btnSubmit.setText("GỬI BÁO CÁO");
                Toast.makeText(ReportIncidentActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

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
                Toast.makeText(ReportIncidentActivity.this, "Gửi báo cáo & ảnh thành công!", Toast.LENGTH_LONG).show();
                finish();
            }
            @Override
            public void onFailure(@NonNull Call<List<String>> call, @NonNull Throwable t) {
                Toast.makeText(ReportIncidentActivity.this, "Đã tạo sự cố nhưng lỗi ảnh", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}