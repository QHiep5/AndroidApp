package com.example.jobhunter.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.jobhunter.R;
import com.example.jobhunter.ViewModel.CompanyViewModel;
import com.example.jobhunter.ViewModel.SkillViewModel;
import com.example.jobhunter.adapter.CompanyAdapter;
import com.example.jobhunter.utils.NavigationManager;
import com.example.jobhunter.utils.SearchHelper;
import com.example.jobhunter.utils.SessionManager;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;

public class CompanyListFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private CompanyViewModel companyViewModel;
    private SkillViewModel skillViewModel;
    private CompanyAdapter companyAdapter;
    private RecyclerView companiesListView;
    private SessionManager sessionManager;

    private TextView etSearch;
    private LinearLayout filterFormContainer, selectedSkillsContainer, searchBarContainer;
    private ChipGroup cgLocation;
    private Button btnApplyFilter;
    private NestedScrollView nestedScrollView;
    private TextView tvSelectSkills;

    private String mParam1;
    private String mParam2;

    public CompanyListFragment() {
        // Required empty public constructor
    }

    public static CompanyListFragment newInstance(String param1, String param2) {
        CompanyListFragment fragment = new CompanyListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_company_list, container, false);

        // Initialize SessionManager
        sessionManager = new SessionManager(getContext());

        // Initialize ViewModels
        companyViewModel = new ViewModelProvider(this).get(CompanyViewModel.class);
        skillViewModel = new ViewModelProvider(this).get(SkillViewModel.class);

        // Initialize Views
        companiesListView = v.findViewById(R.id.company_listview);
        etSearch = v.findViewById(R.id.et_search);
        filterFormContainer = v.findViewById(R.id.filter_form_container);
        cgLocation = v.findViewById(R.id.cg_location);
        btnApplyFilter = v.findViewById(R.id.btn_apply_filter);
        tvSelectSkills = v.findViewById(R.id.tv_select_skills);
        selectedSkillsContainer = v.findViewById(R.id.selected_skills_container);
        nestedScrollView = v.findViewById(R.id.nested_scroll_view);
        searchBarContainer = v.findViewById(R.id.search_bar_container);

        // Setup RecyclerView
        companyAdapter = new CompanyAdapter(getContext(), new ArrayList<>());
        companiesListView.setAdapter(companyAdapter);
        companiesListView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Setup click listeners
        companyAdapter.setOnItemClickListener(company -> {
            Intent intent = new Intent(getContext(), com.example.jobhunter.activity.CompanyDetailActivity.class);
            intent.putExtra("company_id", company.getId());
            startActivity(intent);
        });

        // Fetch data
        String token = sessionManager.getAuthToken();
        companyViewModel.fetchCompanies(token);
        skillViewModel.fetchSkills();

        // Setup observers
        companyViewModel.getCompaniesLiveData().observe(getViewLifecycleOwner(), companies -> {
            companyAdapter.setData(companies);
            Log.d("COMPANY_LIST", "Companies loaded: " + companies.size());
        });

        companyViewModel.getErrorLiveData().observe(getViewLifecycleOwner(), error ->
                Toast.makeText(getContext(), "Lỗi: " + error, Toast.LENGTH_SHORT).show());

        skillViewModel.getErrorLiveData().observe(getViewLifecycleOwner(), error ->
                Toast.makeText(getContext(), "Lỗi kỹ năng: " + error, Toast.LENGTH_SHORT).show());

        // Initialize skills using SearchHelper
        SearchHelper.initializeSkills(skillViewModel, this, tvSelectSkills, selectedSkillsContainer);

        // Setup search and filter
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
                    v,
                    cgLocation,
                    SearchHelper.getSelectedSkillIds(),
                    sessionManager
            );
        });

        return v;
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