package com.example.dalats.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dalats.R;
import com.example.dalats.adapter.IncidentAdapter;
import com.example.dalats.api.ApiClient;
import com.example.dalats.model.Incident;
import com.example.dalats.model.IncidentCategory;
import com.example.dalats.model.IncidentSearchDTO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchIncidentActivity extends AppCompatActivity {

    // UI
    private EditText edtSearch;
    private Spinner spinnerWard, spinnerCategory;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;

    // Data
    private List<Incident> incidentList = new ArrayList<>();
    private IncidentAdapter adapter;
    private List<IncidentCategory> categories = new ArrayList<>();
    private List<String> wards = new ArrayList<>();

    // Pagination State
    private int currentPage = 1;
    private final int PAGE_SIZE = 10;
    private boolean isLoading = false;
    private boolean isLastPage = false;

    // Filter State
    private String currentKeyword = "";
    private String currentWard = null;
    private Integer currentCategoryId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_incident);

        initViews();
        setupData();
        setupEvents();

        // Load lần đầu
        performSearch(true);
    }

    private void initViews() {
        edtSearch = findViewById(R.id.edt_search);
        spinnerWard = findViewById(R.id.spinner_ward);
        spinnerCategory = findViewById(R.id.spinner_category);
        recyclerView = findViewById(R.id.recycler_incidents);
        progressBar = findViewById(R.id.progress_load_more);

        adapter = new IncidentAdapter(this, incidentList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void setupData() {
        // 1. Setup Spinner Phường (Hardcode hoặc lấy từ API/Config)
        wards.add("Tất cả phường");
        wards.addAll(Arrays.asList("Phường 1", "Phường 2", "Phường 3", "Phường 4", "Phường 5", "Phường 6", "Phường 7", "Phường 8", "Phường 9", "Phường 10", "Phường 11", "Phường 12", "Tà Nung", "Trạm Hành", "Xuân Thọ", "Xuân Trường"));

        ArrayAdapter<String> wardAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, wards);
        wardAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerWard.setAdapter(wardAdapter);

        // 2. Setup Spinner Danh mục (Gọi API)
        ApiClient.getIncidentService().getCategories().enqueue(new Callback<List<IncidentCategory>>() {
            @Override
            public void onResponse(Call<List<IncidentCategory>> call, Response<List<IncidentCategory>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    categories = response.body();
                    List<String> catNames = new ArrayList<>();
                    catNames.add("Tất cả danh mục");
                    for(IncidentCategory c : categories) catNames.add(c.getName());

                    ArrayAdapter<String> catAdapter = new ArrayAdapter<>(SearchIncidentActivity.this, android.R.layout.simple_spinner_item, catNames);
                    catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerCategory.setAdapter(catAdapter);
                }
            }
            @Override
            public void onFailure(Call<List<IncidentCategory>> call, Throwable t) {}
        });
    }

    private void setupEvents() {
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // Nút tìm kiếm
        findViewById(R.id.btn_do_search).setOnClickListener(v -> {
            updateFilterParams();
            performSearch(true);
        });

        // Nút lọc
        findViewById(R.id.btn_filter_apply).setOnClickListener(v -> {
            updateFilterParams();
            performSearch(true);
        });

        // Xử lý cuộn để tải thêm (Pagination)
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager lm = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (lm == null) return;

                int visibleItemCount = lm.getChildCount();
                int totalItemCount = lm.getItemCount();
                int firstVisibleItemPosition = lm.findFirstVisibleItemPosition();

                // Nếu chưa loading, chưa phải trang cuối và đã cuộn xuống gần đáy
                if (!isLoading && !isLastPage) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                            && firstVisibleItemPosition >= 0
                            && totalItemCount >= PAGE_SIZE) {
                        currentPage++; // Tăng trang
                        performSearch(false); // Gọi API load tiếp
                    }
                }
            }
        });
    }

    private void updateFilterParams() {
        currentKeyword = edtSearch.getText().toString().trim();

        // Lấy phường
        if (spinnerWard.getSelectedItemPosition() > 0) {
            currentWard = wards.get(spinnerWard.getSelectedItemPosition());
        } else {
            currentWard = null;
        }

        // Lấy danh mục
        if (spinnerCategory.getSelectedItemPosition() > 0 && !categories.isEmpty()) {
            // Index trong list categories = position - 1 (do có item "Tất cả")
            currentCategoryId = categories.get(spinnerCategory.getSelectedItemPosition() - 1).getCategoryId();
        } else {
            currentCategoryId = null;
        }
    }

    private void performSearch(boolean isNewSearch) {
        if (isLoading) return;
        isLoading = true;

        if (isNewSearch) {
            currentPage = 1;
            isLastPage = false;
            incidentList.clear();
            adapter.notifyDataSetChanged();
        } else {
            progressBar.setVisibility(View.VISIBLE);
        }

        IncidentSearchDTO dto = new IncidentSearchDTO(currentKeyword, currentWard, currentCategoryId, currentPage, PAGE_SIZE);

        ApiClient.getIncidentService().searchIncidents(dto).enqueue(new Callback<List<Incident>>() {
            @Override
            public void onResponse(Call<List<Incident>> call, Response<List<Incident>> response) {
                isLoading = false;
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    List<Incident> newItems = response.body();

                    if (newItems.size() < PAGE_SIZE) {
                        isLastPage = true; // Đã hết dữ liệu
                    }

                    incidentList.addAll(newItems);
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(SearchIncidentActivity.this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Incident>> call, Throwable t) {
                isLoading = false;
                progressBar.setVisibility(View.GONE);
                Toast.makeText(SearchIncidentActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }
}