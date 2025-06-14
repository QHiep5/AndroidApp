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

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView rvTopCompanies, rvSuggestedJobs;
    private CompanyAdapter companyAdapter;
    private JobListAdapter jobListAdapter;
    private CompanyViewModel companyViewModel;
    private JobViewModel jobViewModel;
    private SkillViewModel skillViewModel;
    private SessionManager sessionManager;

    private DrawerLayout drawerLayout;
    private NavigationView navView;
    private Toolbar toolbar;

    private TextView etSearch;
    private LinearLayout filterFormContainer, selectedSkillsContainer, searchBarContainer;
    private ChipGroup cgLocation;
    private Button btnApplyFilter;
    private NestedScrollView nestedScrollView;

    private TextView tvSelectSkills;
    private List<Skill> allSkillsList = new ArrayList<>();
    private String[] skillNamesArray = new String[0];
    private boolean[] selectedSkillsFlags;
    private ArrayList<String> selectedSkillIds = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_home, container, false);

        sessionManager = new SessionManager(getContext());

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
        drawerLayout = v.findViewById(R.id.drawer_layout);
        navView = v.findViewById(R.id.nav_view);

        String userRole = sessionManager.getUserRole();
        Log.d("DEBUG_ROLE", "Current user role: " + userRole); // Dòng này sẽ log ra Logcat

        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                requireActivity(), drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Setup navigation menu based on user role
        if (getActivity() != null) {
            NavigationManager.setupNavigationMenu((AppCompatActivity) getActivity(), navView, drawerLayout);
        }

        companyViewModel = new ViewModelProvider(this).get(CompanyViewModel.class);
        jobViewModel = new ViewModelProvider(this).get(JobViewModel.class);
        skillViewModel = new ViewModelProvider(this).get(SkillViewModel.class);
        companyAdapter = new CompanyAdapter(getContext(), new ArrayList<>());
        jobListAdapter = new JobListAdapter(getContext(), new ArrayList<>());

        companyAdapter.setOnItemClickListener(company -> {
            Intent intent = new Intent(getContext(), CompanyDetailActivity.class);
            intent.putExtra("company_id", company.getId());
            startActivity(intent);
        });

        jobListAdapter.setOnJobClickListener(job -> {
            Intent intent = new Intent(getContext(), JobDetailActivity.class);
            intent.putExtra("JOB_ID", job.getId());
            startActivity(intent);
        });

        rvTopCompanies.setAdapter(companyAdapter);
        rvSuggestedJobs.setAdapter(jobListAdapter);
        rvTopCompanies.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvSuggestedJobs.setLayoutManager(new LinearLayoutManager(getContext()));

        String token = sessionManager.getAuthToken();
        companyViewModel.fetchCompanies(token);
        jobViewModel.fetchJobs(token);
        skillViewModel.fetchSkills();

        companyViewModel.getCompaniesLiveData().observe(getViewLifecycleOwner(), companies -> companyAdapter.setData(companies));

        jobViewModel.getJobsLiveData().observe(getViewLifecycleOwner(), jobs -> jobListAdapter.setData(jobs));

        jobViewModel.getErrorLiveData().observe(getViewLifecycleOwner(), error ->
                Toast.makeText(getContext(), "Lỗi: " + error, Toast.LENGTH_SHORT).show());

        skillViewModel.getSkillsLiveData().observe(getViewLifecycleOwner(), skills -> {
            if (skills != null && !skills.isEmpty()) {
                allSkillsList = skills;
                skillNamesArray = new String[skills.size()];
                for (int i = 0; i < skills.size(); i++) {
                    skillNamesArray[i] = skills.get(i).getName();
                }
                selectedSkillsFlags = new boolean[skillNamesArray.length];
            }
        });

        skillViewModel.getErrorLiveData().observe(getViewLifecycleOwner(),
                error -> Toast.makeText(getContext(), "Lỗi: " + error, Toast.LENGTH_SHORT).show());

        searchBarContainer.setOnClickListener(view -> {
            if (filterFormContainer.getVisibility() == View.VISIBLE) hideFilterForm();
            else showFilterForm();
        });

        nestedScrollView.setOnTouchListener((view, event) -> {
            hideFilterForm();
            return false;
        });

        tvSelectSkills.setOnClickListener(view -> showSkillsDialog());

        btnApplyFilter.setOnClickListener(view -> {
            String locationValue = "";
            int checkedId = cgLocation.getCheckedChipId();
            if (checkedId != View.NO_ID) {
                Chip chip = v.findViewById(checkedId);
                switch (chip.getText().toString()) {
                    case "Hà Nội": locationValue = "HANOI"; break;
                    case "Hồ Chí Minh": locationValue = "HOCHIMINH"; break;
                    case "Đà Nẵng": locationValue = "DANANG"; break;
                    case "Other": locationValue = "OTHER"; break;
                }
            }
            Intent intent = new Intent(getActivity(), SearchResultsActivity.class);
            if (!locationValue.isEmpty()) intent.putExtra("location", locationValue);
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
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (getView() != null) imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
        }
        etSearch.clearFocus();
        filterFormContainer.setVisibility(View.GONE);
    }

    private void showSkillsDialog() {
        if (skillNamesArray.length == 0) {
            Toast.makeText(getContext(), "Đang tải kỹ năng...", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Chọn kỹ năng");
        builder.setMultiChoiceItems(skillNamesArray, selectedSkillsFlags, (dialog, i, isChecked) -> selectedSkillsFlags[i] = isChecked);
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
        } else {
            tvSelectSkills.setText(selectedSkillIds.size() + " kỹ năng đã chọn");
        }

        for (int i = 0; i < selectedSkillsFlags.length; i++) {
            if (selectedSkillsFlags[i]) {
                final int index = i;
                Chip chip = new Chip(getContext());
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
}
