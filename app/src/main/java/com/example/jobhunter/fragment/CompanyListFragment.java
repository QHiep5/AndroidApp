package com.example.jobhunter.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.Button;
import android.widget.TextView;

import com.example.jobhunter.R;
import com.example.jobhunter.ViewModel.CompanyViewModel;
import com.example.jobhunter.adapter.CompanyAdapter;

import java.util.ArrayList;

public class CompanyListFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private CompanyViewModel companyViewModel;
    private CompanyAdapter companyAdapter;
    private RecyclerView companiesListView;
    private Button btnPrev, btnNext;
    private TextView tvPageInfo;
    private String mParam1;
    private String mParam2;

    public CompanyListFragment(){
    }

    private void init(){
        this.companyAdapter = new CompanyAdapter(getContext(), new ArrayList<>());
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
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_company_list, container, false);
        companiesListView = v.findViewById(R.id.company_listview);
        btnPrev = v.findViewById(R.id.btn_prev);
        btnNext = v.findViewById(R.id.btn_next);
        tvPageInfo = v.findViewById(R.id.tv_page_info);
        
        init();
        companiesListView.setAdapter(companyAdapter);
        companiesListView.setLayoutManager(new LinearLayoutManager(getContext()));

        companyAdapter.setOnItemClickListener(company -> {
            Intent intent = new Intent(getContext(), com.example.jobhunter.activity.CompanyDetailActivity.class);
            intent.putExtra("company_id", company.getId());
            startActivity(intent);
        });

        companyViewModel = new ViewModelProvider(this).get(CompanyViewModel.class);

        // Setup pagination buttons
        btnPrev.setOnClickListener(v1 -> {
            Integer currentPage = companyViewModel.getCurrentPage().getValue();
            if (currentPage != null && currentPage > 1) {
                companyViewModel.fetchCompanies("", currentPage - 1);
            }
        });

        btnNext.setOnClickListener(v1 -> {
            Integer currentPage = companyViewModel.getCurrentPage().getValue();
            Integer totalPages = companyViewModel.getTotalPages().getValue();
            if (currentPage != null && totalPages != null && currentPage < totalPages) {
                companyViewModel.fetchCompanies("", currentPage + 1);
            }
        });

        // Observe pagination data
        companyViewModel.getCurrentPage().observe(getViewLifecycleOwner(), page -> updatePaginationUI());
        companyViewModel.getTotalPages().observe(getViewLifecycleOwner(), pages -> updatePaginationUI());

        String token = ""; // Lấy token từ SharedPreferences hoặc nơi lưu trữ nếu có
        companyViewModel.fetchCompanies(token);
        companyViewModel.getCompaniesLiveData().observe(getViewLifecycleOwner(), companies -> {
            companyAdapter.setData(companies);
            Log.v("","đây là công ty " + companies);
        });

        return v;
    }

    private void updatePaginationUI() {
        Integer currentPage = companyViewModel.getCurrentPage().getValue();
        Integer totalPages = companyViewModel.getTotalPages().getValue();

        if (currentPage != null && totalPages != null) {
            tvPageInfo.setText(String.format("Page %d of %d", currentPage, totalPages));
            btnPrev.setEnabled(currentPage > 1);
            btnNext.setEnabled(currentPage < totalPages);
        }
    }
}