package com.example.jobhunter.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jobhunter.R;
import com.example.jobhunter.ViewModel.JobViewModel;
import com.example.jobhunter.adapter.JobListAdapter;

import java.util.ArrayList;

public class SearchResultsActivity extends AppCompatActivity {

    private RecyclerView rvSearchResults;
    private JobListAdapter jobListAdapter;
    private JobViewModel viewModel;
    private ProgressBar progressBar;
    private TextView tvNoResults;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);

        Toolbar toolbar = findViewById(R.id.toolbar_search_results);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Kết quả tìm kiếm");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Khởi tạo Views
        rvSearchResults = findViewById(R.id.rv_search_results);
        progressBar = findViewById(R.id.progressBar_search);
        tvNoResults = findViewById(R.id.tv_no_results);

        // Setup RecyclerView
        jobListAdapter = new JobListAdapter(this, new ArrayList<>());
        rvSearchResults.setLayoutManager(new LinearLayoutManager(this));
        rvSearchResults.setAdapter(jobListAdapter);

        // Xử lý sự kiện khi click vào item job
        jobListAdapter.setOnJobClickListener(job -> {
            Intent intent = new Intent(SearchResultsActivity.this, JobDetailActivity.class);
            intent.putExtra("JOB_ID", job.getId());
            startActivity(intent);
        });

        // Lấy dữ liệu lọc từ Intent
        String location = getIntent().getStringExtra("location");
        ArrayList<String> skills = getIntent().getStringArrayListExtra("skills");

        // Khởi tạo ViewModel
        viewModel = new ViewModelProvider(this).get(JobViewModel.class);

        // Quan sát dữ liệu
        viewModel.getFilteredJobs().observe(this, jobs -> {
            progressBar.setVisibility(View.GONE);
            if (jobs != null && !jobs.isEmpty()) {
                Log.d("SEARCH_DEBUG", "Tìm thấy " + jobs.size() + " công việc.");
                jobListAdapter.setData(jobs);
                rvSearchResults.setVisibility(View.VISIBLE);
                tvNoResults.setVisibility(View.GONE);
            } else {
                Log.d("SEARCH_DEBUG", "Không tìm thấy công việc nào hoặc danh sách rỗng.");
                rvSearchResults.setVisibility(View.GONE);
                tvNoResults.setVisibility(View.VISIBLE);
            }
        });

        viewModel.getError().observe(this, error -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            tvNoResults.setVisibility(View.VISIBLE);
            tvNoResults.setText("Đã xảy ra lỗi: " + error);
        });

        // Bắt đầu tìm kiếm
        progressBar.setVisibility(View.VISIBLE);
        viewModel.searchJobs(location, skills);
    }
}
