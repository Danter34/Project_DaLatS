package com.example.dalats.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout; // Import thêm LinearLayout
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.dalats.R;
import com.example.dalats.activity.CreateQuestionActivity;
import com.example.dalats.activity.LoginActivity;
import com.example.dalats.adapter.QAAdapter;
import com.example.dalats.api.ApiClient;
import com.example.dalats.model.QuestionCategory;
import com.example.dalats.model.QuestionResponseDTO;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatFragment extends Fragment {

    // Views
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FloatingActionButton fabAdd;

    // Tabs Components
    // 1. Container (để bắt sự kiện click)
    private LinearLayout btnTabCommunity, btnTabPersonal;
    // 2. Text (để đổi màu chữ)
    private TextView tvTabCommunity, tvTabPersonal;
    // 3. Indicator (gạch chân)
    private View indicatorCommunity, indicatorPersonal;

    // Filters
    private Spinner spinnerCategory;

    // Data
    private List<QuestionResponseDTO> fullList = new ArrayList<>(); // Danh sách gốc từ API
    private List<QuestionResponseDTO> filteredList = new ArrayList<>(); // Danh sách đã lọc
    private List<QuestionCategory> categoryList = new ArrayList<>();

    // State
    private boolean isCommunityTab = true; // Mặc định là Cộng đồng
    private String selectedCategoryName = "Tất cả danh mục"; // Sửa lại mặc định cho khớp logic

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        initViews(view);
        setupTabs();

        // 1. Load danh mục trước
        loadCategories();

        // 2. Load danh sách câu hỏi
        loadData();

        swipeRefreshLayout.setOnRefreshListener(this::loadData);

        fabAdd.setOnClickListener(v -> {
            // Kiểm tra đăng nhập trước khi cho đặt câu hỏi (tùy chọn)
            if (isLoggedIn()) {
                startActivity(new Intent(getActivity(), CreateQuestionActivity.class));
            } else {
                Toast.makeText(getContext(), "Vui lòng đăng nhập để đặt câu hỏi", Toast.LENGTH_SHORT).show();
                // startActivity(new Intent(getActivity(), LoginActivity.class));
            }
        });

        return view;
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_qa);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_qa);
        fabAdd = view.findViewById(R.id.fab_add_question);

        // Ánh xạ đúng ID trong XML
        btnTabCommunity = view.findViewById(R.id.btn_tab_community);
        btnTabPersonal = view.findViewById(R.id.btn_tab_personal);

        tvTabCommunity = view.findViewById(R.id.tv_tab_community);
        tvTabPersonal = view.findViewById(R.id.tv_tab_personal);

        indicatorCommunity = view.findViewById(R.id.indicator_community);
        indicatorPersonal = view.findViewById(R.id.indicator_personal);

        spinnerCategory = view.findViewById(R.id.spinner_filter_category);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    // --- XỬ LÝ CHUYỂN TAB ---
    private void setupTabs() {
        // Bắt sự kiện click vào LinearLayout (vùng bấm rộng hơn)
        btnTabCommunity.setOnClickListener(v -> switchTab(true));
        btnTabPersonal.setOnClickListener(v -> switchTab(false));
    }

    private void switchTab(boolean isCommunity) {
        if (this.isCommunityTab == isCommunity) return;

        this.isCommunityTab = isCommunity;

        // Màu xanh chủ đạo và màu xám
        int colorActive = Color.parseColor("#4CAF50");
        int colorInactive = Color.parseColor("#757575");

        if (isCommunity) {
            // Active Tab Cộng đồng
            tvTabCommunity.setTextColor(colorActive);
            indicatorCommunity.setBackgroundColor(colorActive);

            // Inactive Tab Cá nhân
            tvTabPersonal.setTextColor(colorInactive);
            indicatorPersonal.setBackgroundColor(Color.TRANSPARENT);
        } else {
            // Kiểm tra đăng nhập
            if (!isLoggedIn()) {
                Toast.makeText(getContext(), "Vui lòng đăng nhập để xem câu hỏi của bạn", Toast.LENGTH_SHORT).show();
                this.isCommunityTab = true; // Revert state
                return;
            }

            // Inactive Tab Cộng đồng
            tvTabCommunity.setTextColor(colorInactive);
            indicatorCommunity.setBackgroundColor(Color.TRANSPARENT);

            // Active Tab Cá nhân
            tvTabPersonal.setTextColor(colorActive);
            indicatorPersonal.setBackgroundColor(colorActive);
        }

        // Load lại dữ liệu
        loadData();
    }

    // --- API: Load danh sách câu hỏi ---
    private void loadData() {
        swipeRefreshLayout.setRefreshing(true);

        Call<List<QuestionResponseDTO>> call;

        if (isCommunityTab) {
            call = ApiClient.getQAService().getAllQuestions();
        } else {
            call = ApiClient.getQAService().getMyQuestions();
        }

        call.enqueue(new Callback<List<QuestionResponseDTO>>() {
            @Override
            public void onResponse(Call<List<QuestionResponseDTO>> call, Response<List<QuestionResponseDTO>> response) {
                swipeRefreshLayout.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null) {
                    fullList = response.body();
                    filterListByCategory();
                } else {
                    fullList.clear();
                    updateRecyclerView(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(Call<List<QuestionResponseDTO>> call, Throwable t) {
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(getContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --- API: Load danh mục ---
    private void loadCategories() {
        ApiClient.getQAService().getCategories().enqueue(new Callback<List<QuestionCategory>>() {
            @Override
            public void onResponse(Call<List<QuestionCategory>> call, Response<List<QuestionCategory>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    categoryList = response.body();
                    setupSpinner();
                }
            }
            @Override
            public void onFailure(Call<List<QuestionCategory>> call, Throwable t) {}
        });
    }

    private void setupSpinner() {
        if (getContext() == null) return;

        List<String> displayList = new ArrayList<>();
        displayList.add("Tất cả danh mục");
        for (QuestionCategory c : categoryList) {
            displayList.add(c.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, displayList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);

        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    selectedCategoryName = "Tất cả danh mục";
                } else {
                    selectedCategoryName = categoryList.get(position - 1).getName();
                }
                filterListByCategory();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void filterListByCategory() {
        if (selectedCategoryName.equals("Tất cả danh mục")) {
            filteredList = new ArrayList<>(fullList);
        } else {
            filteredList = new ArrayList<>();
            for (QuestionResponseDTO item : fullList) {
                // Kiểm tra null an toàn và so sánh chuỗi
                if (item.getQuestionCategoryName() != null &&
                        item.getQuestionCategoryName().equalsIgnoreCase(selectedCategoryName)) {
                    filteredList.add(item);
                }
            }
        }
        updateRecyclerView(filteredList);
    }

    private void updateRecyclerView(List<QuestionResponseDTO> list) {
        if (getContext() != null) {
            QAAdapter adapter = new QAAdapter(getContext(), list);
            recyclerView.setAdapter(adapter);
        }
    }

    private boolean isLoggedIn() {
        if (getContext() == null) return false;
        SharedPreferences pref = getContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        return pref.getString("TOKEN", null) != null;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }
}