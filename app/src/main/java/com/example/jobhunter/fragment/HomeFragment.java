package com.example.jobhunter.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.core.widget.NestedScrollView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jobhunter.R;
import com.example.jobhunter.ViewModel.CompanyViewModel;
import com.example.jobhunter.ViewModel.JobViewModel;
import com.example.jobhunter.ViewModel.SkillViewModel;
import com.example.jobhunter.activity.CvManagementActivity;
import com.example.jobhunter.activity.JobDetailActivity;
import com.example.jobhunter.activity.SearchResultsActivity;
import com.example.jobhunter.activity.CompanyDetailActivity;
import com.example.jobhunter.adapter.CompanyAdapter;
import com.example.jobhunter.adapter.JobListAdapter;
import com.example.jobhunter.model.Skill;
import com.example.jobhunter.utils.SessionManager;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.navigation.NavigationView;
import com.example.jobhunter.utils.NavigationManager;
import com.example.jobhunter.utils.SearchHelper;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";

    private RecyclerView rvTopCompanies, rvSuggestedJobs;
    private CompanyAdapter companyAdapter;
    private JobListAdapter jobListAdapter;
    private CompanyViewModel companyViewModel;
    private JobViewModel jobViewModel;
    private SkillViewModel skillViewModel;
    private SessionManager sessionManager;

    private TextView etSearch;
    private LinearLayout filterFormContainer, selectedSkillsContainer, searchBarContainer;
    private ChipGroup cgLocation;
    private Button btnApplyFilter;
    private NestedScrollView nestedScrollView;

    private TextView tvSelectSkills;

    public HomeFragment() {
        // Constructor mặc định
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Khởi tạo các ViewModel
        companyViewModel = new ViewModelProvider(this).get(CompanyViewModel.class);
        jobViewModel = new ViewModelProvider(this).get(JobViewModel.class);
        skillViewModel = new ViewModelProvider(this).get(SkillViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, container, false);

        // Khởi tạo SessionManager
        sessionManager = new SessionManager(getContext());

        // Khởi tạo các Views
        initializeViews(v);

        // Thiết lập RecyclerView
        setupRecyclerView();

        // Thiết lập các observers
        setupObservers();

        // Thiết lập search và filter
        setupSearchAndFilter();

        return v;
    }

    private void initializeViews(View v) {
        // Khởi tạo các views
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
    }

    private void setupRecyclerView() {
        // Thiết lập RecyclerView cho companies
        companyAdapter = new CompanyAdapter(getContext(), new ArrayList<>());
        rvTopCompanies.setAdapter(companyAdapter);
        rvTopCompanies.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        // Thiết lập RecyclerView cho jobs
        jobListAdapter = new JobListAdapter(getContext(), new ArrayList<>());
        rvSuggestedJobs.setAdapter(jobListAdapter);
        rvSuggestedJobs.setLayoutManager(new LinearLayoutManager(getContext()));

        // Xử lý click vào company
        companyAdapter.setOnItemClickListener(company -> {
            Intent intent = new Intent(getContext(), CompanyDetailActivity.class);
            intent.putExtra("company_id", company.getId());
            startActivity(intent);
        });

        // Xử lý click vào job
        jobListAdapter.setOnJobClickListener(job -> {
            Intent intent = new Intent(getContext(), JobDetailActivity.class);
            intent.putExtra("JOB_ID", job.getId());
            startActivity(intent);
        });
    }

    private void setupObservers() {
        // Observer cho danh sách companies
        companyViewModel.getCompaniesLiveData().observe(getViewLifecycleOwner(), companies -> {
            companyAdapter.setData(companies);
            Log.d("HOME", "Companies loaded: " + companies.size());
        });

        // Observer cho danh sách jobs
        jobViewModel.getJobsLiveData().observe(getViewLifecycleOwner(), jobs -> {
            jobListAdapter.setData(jobs);
            Log.d("HOME", "Jobs loaded: " + jobs.size());
        });

        // Observer cho lỗi jobs
        jobViewModel.getErrorLiveData().observe(getViewLifecycleOwner(), error ->
                Toast.makeText(getContext(), "Lỗi: " + error, Toast.LENGTH_SHORT).show());

        // Observer cho lỗi skills
        skillViewModel.getErrorLiveData().observe(getViewLifecycleOwner(), error ->
                Toast.makeText(getContext(), "Lỗi kỹ năng: " + error, Toast.LENGTH_SHORT).show());

        // Fetch dữ liệu
        String token = sessionManager.getAuthToken();
        companyViewModel.fetchCompanies(token);
        jobViewModel.fetchJobs(token);
        skillViewModel.fetchSkills();
    }

    private void setupSearchAndFilter() {
        // Khởi tạo skills sử dụng SearchHelper
        SearchHelper.initializeSkills(skillViewModel, this, tvSelectSkills, selectedSkillsContainer);

        // Xử lý click vào search bar
        searchBarContainer.setOnClickListener(view -> {
            if (filterFormContainer.getVisibility() == View.VISIBLE)
                SearchHelper.hideFilterForm(filterFormContainer, etSearch, getContext(), getView());
            else
                SearchHelper.showFilterForm(filterFormContainer);
        });

        // Xử lý touch vào nested scroll view
        nestedScrollView.setOnTouchListener((view, event) -> {
            SearchHelper.hideFilterForm(filterFormContainer, etSearch, getContext(), getView());
            return false;
        });

        // Xử lý click vào nút apply filter
        btnApplyFilter.setOnClickListener(view -> {
            SearchHelper.handleSearch(
                    this,
                    getView(),
                    cgLocation,
                    SearchHelper.getSelectedSkillIds(),
                    sessionManager
            );
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        // Reset dữ liệu tìm kiếm khi rời khỏi fragment
        SearchHelper.resetAllData();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reset UI khi quay lại fragment
        if (etSearch != null) {
            etSearch.setText("");
        }
        if (cgLocation != null) {
            cgLocation.clearCheck();
        }
        if (tvSelectSkills != null) {
            tvSelectSkills.setText("Chọn kỹ năng...");
        }
        if (selectedSkillsContainer != null) {
            selectedSkillsContainer.removeAllViews();
        }
        // Ẩn filterForm khi quay lại fragment
        if (filterFormContainer != null) {
            filterFormContainer.setVisibility(View.GONE);
        }
    }
}