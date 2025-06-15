package com.example.jobhunter.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ViewFlipper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.jobhunter.R;
import com.example.jobhunter.activity.JobDetailActivity;
import com.example.jobhunter.adapter.JobListAdapter;
import com.example.jobhunter.model.Skill;
import com.example.jobhunter.utils.SessionManager;
import com.example.jobhunter.ViewModel.JobViewModel;
import com.example.jobhunter.utils.ToolbarManager;
import java.util.ArrayList;
import java.util.List;

import android.util.Log;
import android.widget.Toast;
import android.widget.Button;
import android.widget.TextView;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.navigation.NavigationView;
import com.example.jobhunter.ViewModel.SkillViewModel;
import com.example.jobhunter.utils.NavigationManager;
import com.example.jobhunter.utils.SearchHelper;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.core.widget.NestedScrollView;
import android.view.MotionEvent;

public class JobListFragment extends Fragment {

    private static final String TAG = "JobListFragment";

    private JobViewModel jobViewModel;
    private SkillViewModel skillViewModel;
    private JobListAdapter jobListAdapter;
    private RecyclerView rvSuggestedJobs;

    private SessionManager sessionManager;
    private Button btnPrev, btnNext;
    private TextView tvPageInfo;

    private TextView etSearch;
    private LinearLayout filterFormContainer, selectedSkillsContainer, searchBarContainer;
    private ChipGroup cgLocation;
    private Button btnApplyFilter;
    private NestedScrollView nestedScrollView;
    private TextView tvSelectSkills;

    private ArrayList<String> selectedSkillIds = new ArrayList<>();
    private boolean[] selectedSkillsFlags;
    private List<Skill> allSkillsList = new ArrayList<>();

    public JobListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        jobViewModel = new ViewModelProvider(this).get(JobViewModel.class);
        skillViewModel = new ViewModelProvider(this).get(SkillViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_job_list, container, false);

        // Initialize SessionManager
        sessionManager = new SessionManager(getContext());

        // Initialize Views
        drawerLayout = view.findViewById(R.id.drawer_layout);
        toolbar = view.findViewById(R.id.toolbar);
        viewFlipper = view.findViewById(R.id.viewFlipper);
        rvSuggestedJobs = view.findViewById(R.id.rv_suggested_jobs);
        btnPrev = view.findViewById(R.id.btn_prev);
        btnNext = view.findViewById(R.id.btn_next);
        tvPageInfo = view.findViewById(R.id.tv_page_info);
        initializeViews(v);

        // Setup RecyclerView
        setupRecyclerView();
        
        // Setup observers
        setupObservers();
        
        // Initialize skills and search
        setupSearchAndFilter();

        return v;
    }

    private void initializeViews(View v) {
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
        jobListAdapter = new JobListAdapter(getContext(), new ArrayList<>());
        rvSuggestedJobs.setAdapter(jobListAdapter);
        rvSuggestedJobs.setLayoutManager(new LinearLayoutManager(getContext()));

        jobListAdapter.setOnJobClickListener(job -> {
            Intent intent = new Intent(getContext(), com.example.jobhunter.activity.JobDetailActivity.class);
            intent.putExtra("JOB_ID", job.getId());
            startActivity(intent);
        });
    

        // Setup pagination buttons
        btnPrev.setOnClickListener(v -> {
            Integer currentPage = jobViewModel.getCurrentPage().getValue();
            if (currentPage != null && currentPage > 1) {
                jobViewModel.fetchJobs(sessionManager.getAuthToken(), currentPage - 1);
            }
        });

        btnNext.setOnClickListener(v -> {
            Integer currentPage = jobViewModel.getCurrentPage().getValue();
            Integer totalPages = jobViewModel.getTotalPages().getValue();
            if (currentPage != null && totalPages != null && currentPage < totalPages) {
                jobViewModel.fetchJobs(sessionManager.getAuthToken(), currentPage + 1);
            }
        });

        return view;
    }

    private void setupObservers() {
        jobViewModel.getJobsLiveData().observe(getViewLifecycleOwner(), jobs -> {
                jobListAdapter.setData(jobs);
            Log.d("JOB_LIST", "Jobs loaded: " + jobs.size());
        });

        jobViewModel.getErrorLiveData().observe(getViewLifecycleOwner(), error ->
            Toast.makeText(getContext(), "Lỗi: " + error, Toast.LENGTH_SHORT).show());

        skillViewModel.getSkillsLiveData().observe(getViewLifecycleOwner(), skills -> {
            allSkillsList = skills;
            selectedSkillsFlags = new boolean[skills.size()];
            String[] skillNames = new String[skills.size()];
            for (int i = 0; i < skills.size(); i++) {
                skillNames[i] = skills.get(i).getName();
            }
            tvSelectSkills.setOnClickListener(v -> showSkillsDialog(skillNames));
        });

        skillViewModel.getErrorLiveData().observe(getViewLifecycleOwner(), error ->
            Toast.makeText(getContext(), "Lỗi kỹ năng: " + error, Toast.LENGTH_SHORT).show());

        String token = sessionManager.getAuthToken();
        jobViewModel.fetchJobs(token);
        skillViewModel.fetchSkills();
    }

    private void setupSearchAndFilter() {
        SearchHelper.initializeSkills(skillViewModel, this, tvSelectSkills, selectedSkillsContainer);

        searchBarContainer.setOnClickListener(view -> {
            if (filterFormContainer.getVisibility() == View.VISIBLE)
                SearchHelper.hideFilterForm(filterFormContainer, etSearch, getContext(), getView());
            else
                SearchHelper.showFilterForm(filterFormContainer);
        });

        nestedScrollView.setOnTouchListener((view, event) -> {
            SearchHelper.hideFilterForm(filterFormContainer, etSearch, getContext(), getView());
            return false;
        });

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

    private void showSkillsDialog(String[] skillNames) {
        if (skillNames.length == 0) {
            Toast.makeText(getContext(), "Đang tải kỹ năng...", Toast.LENGTH_SHORT).show();
            return;
        }

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
        builder.setTitle("Chọn kỹ năng");
        builder.setMultiChoiceItems(skillNames, selectedSkillsFlags, (dialog, i, isChecked) -> selectedSkillsFlags[i] = isChecked);
        builder.setPositiveButton("OK", (dialog, which) -> {
            selectedSkillIds.clear();
            for (int i = 0; i < selectedSkillsFlags.length; i++) {
                if (selectedSkillsFlags[i]) {
                    selectedSkillIds.add(String.valueOf(allSkillsList.get(i).getId()));
                }
            }
            updateSelectedSkillsView();
        });
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
        builder.setNeutralButton("Xóa tất cả", (dialog, which) -> {
            for (int i = 0; i < selectedSkillsFlags.length; i++) selectedSkillsFlags[i] = false;
            selectedSkillIds.clear();
            updateSelectedSkillsView();
        });
        builder.show();
    }

    private void updateSelectedSkillsView() {
        selectedSkillsContainer.removeAllViews();
        if (selectedSkillIds.isEmpty()) {
            tvSelectSkills.setText("Chọn kỹ năng...");
        }
        else {
            tvSelectSkills.setText(selectedSkillIds.size() + " kỹ năng đã chọn");
        }
        for (int i = 0; i < selectedSkillsFlags.length; i++) {
            if (selectedSkillsFlags[i]) {
                final int index = i;
                com.google.android.material.chip.Chip chip = new com.google.android.material.chip.Chip(getContext());
                chip.setText(allSkillsList.get(i).getName());
                chip.setCloseIconVisible(true);
                chip.setOnCloseIconClickListener(v -> {
                    selectedSkillsFlags[index] = false;
                    selectedSkillIds.remove(String.valueOf(allSkillsList.get(index).getId()));
                    updateSelectedSkillsView();
                });
                selectedSkillsContainer.addView(chip);
            }
        }
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
        viewFlipper.setFlipInterval(3000);
        viewFlipper.setAutoStart(true);
        viewFlipper.setInAnimation(getContext(), android.R.anim.fade_in);
        viewFlipper.setOutAnimation(getContext(), android.R.anim.fade_out);

        // Setup ViewModel and Observe Data
        jobViewModel = new ViewModelProvider(this).get(JobViewModel.class);
        Log.d(TAG, "ViewModel and SessionManager initialized.");

        jobViewModel.getJobsLiveData().observe(getViewLifecycleOwner(), jobs -> {
            if (jobs != null) {
                Log.d(TAG, "Jobs LiveData updated. Received " + jobs.size() + " jobs. Submitting to adapter.");
                jobListAdapter.setData(jobs);
            } else {
                Log.w(TAG, "Jobs LiveData updated with null list.");
            }
        });

        jobViewModel.getErrorLiveData().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Log.e(TAG, "Error LiveData updated: " + error);
                Toast.makeText(getContext(), "Lỗi: " + error, Toast.LENGTH_LONG).show();
            }
        });

        // Observe pagination data
        jobViewModel.getCurrentPage().observe(getViewLifecycleOwner(), page -> updatePaginationUI());
        jobViewModel.getTotalPages().observe(getViewLifecycleOwner(), pages -> updatePaginationUI());

        sessionManager = new SessionManager(getContext());
        String token = sessionManager.getAuthToken();
        if (token == null || token.isEmpty()) {
            Log.w(TAG, "Authentication token is NULL or EMPTY. Fetching jobs without auth.");
        } else {
            Log.i(TAG, "Authentication token found. Fetching jobs with token.");
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

    private void updatePaginationUI() {
        Integer currentPage = jobViewModel.getCurrentPage().getValue();
        Integer totalPages = jobViewModel.getTotalPages().getValue();

        if (currentPage != null && totalPages != null) {
            tvPageInfo.setText(String.format("Page %d of %d", currentPage, totalPages));
            btnPrev.setEnabled(currentPage > 1);
            btnNext.setEnabled(currentPage < totalPages);
        }
    }
}