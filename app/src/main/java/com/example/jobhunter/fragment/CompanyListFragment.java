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
        init();
        companiesListView.setAdapter(companyAdapter);
        companiesListView.setLayoutManager(new LinearLayoutManager(getContext()));

        companyAdapter.setOnItemClickListener(company -> {
            Intent intent = new Intent(getContext(), com.example.jobhunter.activity.CompanyDetailActivity.class);
            intent.putExtra("company_id", company.getId());
            startActivity(intent);
        });

        companyViewModel = new ViewModelProvider(this).get(CompanyViewModel.class);

        String token = ""; // Lấy token từ SharedPreferences hoặc nơi lưu trữ nếu có
        companyViewModel.fetchCompanies(token);
        companyViewModel.getCompaniesLiveData().observe(getViewLifecycleOwner(), companies -> {
            companyAdapter.setData(companies);
            Log.v("","đây là công ty " + companies);
        });

        return v;
    }
}