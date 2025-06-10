package com.example.jobhunter.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.jobhunter.R;
import com.example.jobhunter.ViewModel.HomeViewModel;
import com.example.jobhunter.ViewModel.JobViewModel;
import com.example.jobhunter.adapter.CompanyAdapter;
import com.example.jobhunter.adapter.JobListAdapter;
import java.util.ArrayList;

public class HomeFragment extends Fragment {
    private RecyclerView rvTopCompanies, rvSuggestedJobs;
    private CompanyAdapter companyAdapter;
    private JobListAdapter jobListAdapter;
    private HomeViewModel homeViewModel;
    private JobViewModel jobViewModel;
    private ViewFlipper viewFlipper;
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {


        View v = inflater.inflate(R.layout.fragment_home, container, false);
        rvTopCompanies = v.findViewById(R.id.rv_top_companies);
        rvSuggestedJobs = v.findViewById(R.id.rv_suggested_jobs);

        companyAdapter = new CompanyAdapter(getContext(), new ArrayList<>());
        jobListAdapter = new JobListAdapter(getContext(), new ArrayList<>());

        rvTopCompanies.setAdapter(companyAdapter);
        rvSuggestedJobs.setAdapter(jobListAdapter);
        rvTopCompanies.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvSuggestedJobs.setLayoutManager(new LinearLayoutManager(getContext()));

        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        homeViewModel.getTopCompanies().observe(getViewLifecycleOwner(), companies -> {
            companyAdapter.setData(companies);
        });
        homeViewModel.getSuggestedJobs().observe(getViewLifecycleOwner(), jobs -> {
            jobListAdapter.setData(jobs);
        });
        homeViewModel.getError().observe(getViewLifecycleOwner(), error -> {
            Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
        });

        String token = ""; // Lấy token từ SharedPreferences hoặc nơi lưu trữ
        homeViewModel.fetchTopCompanies(getContext(), token);
        homeViewModel.fetchSuggestedJobs(getContext(), token);

        return v;
    }
}