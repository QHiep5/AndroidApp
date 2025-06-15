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
import com.example.jobhunter.model.Job;
import com.example.jobhunter.model.Company;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.List;
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

        // Lấy dữ liệu search từ intent
        String searchResultJson = getIntent().getStringExtra("searchResultJson");
        if (searchResultJson != null) {
            // Parse JSON và hiển thị kết quả
            try {
                JSONObject response = new JSONObject(searchResultJson);
                List<Job> jobs = new ArrayList<>();
                JSONObject data = response.getJSONObject("data");
                JSONArray result = data.getJSONArray("result");
                for (int i = 0; i < result.length(); i++) {
                    JSONObject obj = result.getJSONObject(i);
                    Job job = new Job();
                    job.setId(obj.optInt("id"));
                    job.setName(obj.optString("name"));
                    job.setLocation(obj.optString("location"));
                    job.setSalary(obj.optDouble("salary"));
                    // Parse các trường khác nếu cần...
                    JSONObject companyObj = obj.optJSONObject("company");
                    if (companyObj != null) {
                        Company company = new Company();
                        company.setName(companyObj.optString("name"));
                        company.setLogo(companyObj.optString("logo"));
                        job.setCompany(company);
                    }
                    jobs.add(job);
                }
                jobListAdapter.setData(jobs);
                progressBar.setVisibility(View.GONE);
                rvSearchResults.setVisibility(jobs.isEmpty() ? View.GONE : View.VISIBLE);
                tvNoResults.setVisibility(jobs.isEmpty() ? View.VISIBLE : View.GONE);
            } catch (Exception e) {
                progressBar.setVisibility(View.GONE);
                tvNoResults.setVisibility(View.VISIBLE);
                tvNoResults.setText("Lỗi khi phân tích kết quả tìm kiếm.");
            }
        } else {
            // Fallback: Cách cũ
        String location = getIntent().getStringExtra("location");
        ArrayList<String> skills = getIntent().getStringArrayListExtra("skills");
        viewModel = new ViewModelProvider(this).get(JobViewModel.class);
        viewModel.getFilteredJobs().observe(this, jobs -> {
            progressBar.setVisibility(View.GONE);
            if (jobs != null && !jobs.isEmpty()) {
                jobListAdapter.setData(jobs);
                rvSearchResults.setVisibility(View.VISIBLE);
                tvNoResults.setVisibility(View.GONE);
            } else {
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
        progressBar.setVisibility(View.VISIBLE);
        viewModel.searchJobs(location, skills);
        }
    }
}
