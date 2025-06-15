package com.example.jobhunter.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.jobhunter.R;
import com.example.jobhunter.ViewModel.JobViewModel;
import com.example.jobhunter.adapter.JobManageAdapter;
import com.example.jobhunter.model.Job;
import java.util.ArrayList;

public class JobManageActivity extends AppCompatActivity {
    private ImageView btnBack;
    private EditText etSearchCompany;
    private Button btnAddJob, btnSearch;
    private RecyclerView rvJobs;
    private JobManageAdapter adapter;
    private JobViewModel jobViewModel;
    private ArrayList<Job> allJobs = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_manager);

        // Ánh xạ view
        btnBack = findViewById(R.id.btn_back);
        etSearchCompany = findViewById(R.id.et_search_company);
        btnAddJob = findViewById(R.id.btn_add_job);
        btnSearch = findViewById(R.id.btn_search);

        rvJobs = findViewById(R.id.rv_jobs);

        // Xử lý nút quay lại
        btnBack.setOnClickListener(v -> onBackPressed());

        // Khởi tạo adapter
        adapter = new JobManageAdapter(new ArrayList<>(), new JobManageAdapter.OnActionClickListener() {
            @Override
            public void onEdit(Job job) {
                Toast.makeText(JobManageActivity.this, "Sửa: " + job.getName(), Toast.LENGTH_SHORT).show();
                // TODO: Mở màn hình sửa job
            }
            @Override
            public void onDelete(Job job) {
                Toast.makeText(JobManageActivity.this, "Xoá: " + job.getName(), Toast.LENGTH_SHORT).show();
                // TODO: Xử lý xoá job
            }
        });
        rvJobs.setLayoutManager(new LinearLayoutManager(this));
        rvJobs.setAdapter(adapter);

        // ViewModel
        jobViewModel = new ViewModelProvider(this).get(JobViewModel.class);
        String token = new com.example.jobhunter.utils.SessionManager(this).getAuthToken();
        jobViewModel.fetchJobs(token);

        // Quan sát dữ liệu
        jobViewModel.getJobsLiveData().observe(this, jobs -> {
            if (jobs != null) {
                allJobs.clear();
                allJobs.addAll(jobs);
                adapter.setData(jobs);
            }
        });

        // Tìm kiếm (lọc theo tên công ty)
        btnSearch.setOnClickListener(v -> {
            String keyword = etSearchCompany.getText().toString().trim().toLowerCase();
            ArrayList<Job> filtered = new ArrayList<>();
            for (Job job : allJobs) {
                if (job.getCompany() != null && job.getCompany().getName() != null &&
                    job.getCompany().getName().toLowerCase().contains(keyword)) {
                    filtered.add(job);
                }
            }
            adapter.setData(filtered);
        });

        // Thêm job
        btnAddJob.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddJobActivity.class);
            startActivity(intent);
        });
    }
}
