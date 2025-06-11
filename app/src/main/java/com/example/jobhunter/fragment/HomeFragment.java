package com.example.jobhunter.fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jobhunter.R;
import com.example.jobhunter.ViewModel.HomeViewModel;
import com.example.jobhunter.ViewModel.JobViewModel;
import com.example.jobhunter.ViewModel.SkillViewModel;
import com.example.jobhunter.activity.SearchResultsActivity;
import com.example.jobhunter.adapter.CompanyAdapter;
import com.example.jobhunter.adapter.JobListAdapter;
import com.example.jobhunter.model.Skill;
import com.example.jobhunter.utils.ToolbarManager;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView rvTopCompanies, rvSuggestedJobs;
    private CompanyAdapter companyAdapter;
    private JobListAdapter jobListAdapter;
    private HomeViewModel homeViewModel;
    private JobViewModel jobViewModel;
    private SkillViewModel skillViewModel;
    private ViewFlipper viewFlipper;
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private TextView etSearch;
    private LinearLayout filterFormContainer;
    private ChipGroup cgLocation;
    private Button btnApplyFilter;

    // --- Dữ liệu cho việc chọn kỹ năng ---
    private TextView tvSelectSkills;
    private LinearLayout selectedSkillsContainer;
    // Lưu danh sách các đối tượng Skill đầy đủ
    private List<Skill> allSkillsList = new ArrayList<>();
    // Mảng tên kỹ năng để hiển thị trong dialog
    private String[] skillNamesArray = new String[0];
    private boolean[] selectedSkillsFlags;
    // Lưu danh sách các ID kỹ năng đã chọn
    private ArrayList<String> selectedSkillIds = new ArrayList<>();
    private NestedScrollView nestedScrollView;
    private LinearLayout searchBarContainer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_home, container, false);
        rvTopCompanies = v.findViewById(R.id.rv_top_companies);
        rvSuggestedJobs = v.findViewById(R.id.rv_suggested_jobs);
        etSearch = v.findViewById(R.id.et_search);
        filterFormContainer = v.findViewById(R.id.filter_form_container);
        cgLocation = v.findViewById(R.id.cg_location);
        btnApplyFilter = v.findViewById(R.id.btn_apply_filter);
        tvSelectSkills = v.findViewById(R.id.tv_select_skills);
        selectedSkillsContainer = v.findViewById(R.id.selected_skills_container);
        nestedScrollView = v.findViewById(R.id.nested_scroll_view);
        searchBarContainer = v.findViewById(R.id.search_bar_container);
        toolbar = v.findViewById(R.id.toolbar);

        // --- Thiết lập Toolbar bằng ToolbarManager ---
        ToolbarManager.setupToolbarWithDrawer((AppCompatActivity) getActivity(), toolbar);

        companyAdapter = new CompanyAdapter(getContext(), new ArrayList<>());
        jobListAdapter = new JobListAdapter(getContext(), new ArrayList<>());

        rvTopCompanies.setAdapter(companyAdapter);
        rvSuggestedJobs.setAdapter(jobListAdapter);
        rvTopCompanies.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvSuggestedJobs.setLayoutManager(new LinearLayoutManager(getContext()));

        // --- Tích hợp ViewModel ---
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        skillViewModel = new ViewModelProvider(this).get(SkillViewModel.class);

        // Lấy dữ liệu
        homeViewModel.fetchTopCompanies(getContext());
        homeViewModel.fetchSuggestedJobs(getContext());
        skillViewModel.fetchSkills(); // Gọi API lấy danh sách kỹ năng

        homeViewModel.getTopCompanies().observe(getViewLifecycleOwner(), companies -> {
            companyAdapter.setData(companies);
        });
        homeViewModel.getSuggestedJobs().observe(getViewLifecycleOwner(), jobs -> {
            jobListAdapter.setData(jobs);
        });

        // Lắng nghe kết quả trả về từ SkillViewModel
        skillViewModel.getSkillsLiveData().observe(getViewLifecycleOwner(), skills -> {
            if (skills != null && !skills.isEmpty()) {
                allSkillsList = skills; // Lưu lại danh sách đầy đủ
                // Tạo mảng tên để hiển thị
                skillNamesArray = new String[skills.size()];
                for (int i = 0; i < skills.size(); i++) {
                    skillNamesArray[i] = skills.get(i).getName();
                }
                // Khởi tạo lại mảng boolean cho lựa chọn
                selectedSkillsFlags = new boolean[skillNamesArray.length];
            }
        });

        skillViewModel.getErrorLiveData().observe(getViewLifecycleOwner(), error -> {
            Log.e("HOME_FRAGMENT", "Lỗi từ SkillViewModel: " + error);
            Toast.makeText(getContext(), "Lỗi khi tải kỹ năng: " + error, Toast.LENGTH_SHORT).show();
        });

        // --- Logic ẩn/hiện form lọc ---
        searchBarContainer.setOnClickListener(view -> {
            // Chuyển đổi trạng thái hiển thị của form lọc
            if (filterFormContainer.getVisibility() == View.VISIBLE) {
                hideFilterForm();
            } else {
                showFilterForm();
            }
        });

        // Bấm ra ngoài để ẩn form
        nestedScrollView.setOnTouchListener((view, motionEvent) -> {
            hideFilterForm();
            return false; // Trả về false để không cản trở sự kiện cuộn
        });

        tvSelectSkills.setOnClickListener(view -> showSkillsDialog());

        btnApplyFilter.setOnClickListener(view -> {
            String locationValue = ""; // Giá trị sẽ được gửi đi
            int checkedLocationId = cgLocation.getCheckedChipId();

            if (checkedLocationId != View.NO_ID) {
                Chip selectedLocationChip = v.findViewById(checkedLocationId);
                String locationText = selectedLocationChip.getText().toString();
                switch (locationText) {
                    case "Hà Nội":
                        locationValue = "HANOI";
                        break;
                    case "Hồ Chí Minh":
                        locationValue = "HOCHIMINH";
                        break;
                    case "Đà Nẵng":
                        locationValue = "DANANG";
                        break;
                    case "Other":
                        locationValue = "OTHER";
                        break;
                }
            }

            // `selectedSkillIds` đã được cập nhật trong showSkillsDialog

            Intent intent = new Intent(getActivity(), SearchResultsActivity.class);
            // Gửi đi giá trị đã được ánh xạ
            if (!locationValue.isEmpty()) {
                intent.putExtra("location", locationValue);
            }
            intent.putStringArrayListExtra("skills", selectedSkillIds);
            startActivity(intent);
        });

        return v;
    }

    private void showFilterForm() {
        filterFormContainer.setVisibility(View.VISIBLE);
    }

    private void hideFilterForm() {
        if (getContext() != null) {
            // Ẩn bàn phím
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (getView() != null) {
                imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
            }
        }
        // Xóa focus khỏi EditText và ẩn form
        etSearch.clearFocus();
        filterFormContainer.setVisibility(View.GONE);
    }

    private void showSkillsDialog() {
        if (skillNamesArray == null || skillNamesArray.length == 0) {
            Toast.makeText(getContext(), "Đang tải danh sách kỹ năng...", Toast.LENGTH_SHORT).show();
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Chọn kỹ năng");
        builder.setCancelable(false);

        builder.setMultiChoiceItems(skillNamesArray, selectedSkillsFlags, (dialogInterface, i, isChecked) -> {
            selectedSkillsFlags[i] = isChecked;
        });

        builder.setPositiveButton("OK", (dialog, which) -> {
            selectedSkillIds.clear();
            for (int i = 0; i < selectedSkillsFlags.length; i++) {
                if (selectedSkillsFlags[i]) {
                    // Lấy ID từ đối tượng Skill tương ứng và thêm vào danh sách
                    selectedSkillIds.add(String.valueOf(allSkillsList.get(i).getId()));
                }
            }
            updateSelectedSkillsView();
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());

        builder.setNeutralButton("Xóa tất cả", (dialog, which) -> {
            for (int i = 0; i < selectedSkillsFlags.length; i++) {
                selectedSkillsFlags[i] = false;
            }
            selectedSkillIds.clear();
            updateSelectedSkillsView();
        });

        builder.show();
    }

    private void updateSelectedSkillsView() {
        selectedSkillsContainer.removeAllViews(); // Xóa các chip cũ

        if (selectedSkillIds.isEmpty()) {
            tvSelectSkills.setText("Chọn kỹ năng...");
        } else {
            tvSelectSkills.setText(String.format("%d kỹ năng đã chọn", selectedSkillIds.size()));
        }

        // Hiển thị Chip dựa trên tên, nhưng chúng ta đã lưu ID
        for (int i = 0; i < selectedSkillsFlags.length; i++) {
            if (selectedSkillsFlags[i]) {
                final int selectedIndex = i;
                Chip chip = new Chip(getContext());
                chip.setText(allSkillsList.get(i).getName());
                chip.setCloseIconVisible(true);
                chip.setOnCloseIconClickListener(v -> {
                    // Xóa kỹ năng khỏi danh sách và cập nhật lại view
                    selectedSkillsFlags[selectedIndex] = false;
                    selectedSkillIds.remove(String.valueOf(allSkillsList.get(selectedIndex).getId()));
                    updateSelectedSkillsView();
                });
                selectedSkillsContainer.addView(chip);
            }
        }
    }
}
