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
                Intent intent = new Intent(JobManageActivity.this, UpdateJobActivity.class);
                intent.putExtra("JOB_ID", job.getId());
                startActivity(intent);
            }
            @Override
            public void onDelete(Job job) {
                android.util.Log.d("JobManageActivity", "Xoá job với ID: " + job.getId());
                new androidx.appcompat.app.AlertDialog.Builder(JobManageActivity.this)
                    .setTitle("Xác nhận xoá")
                    .setMessage("Bạn có chắc muốn xoá công việc này?")
                    .setPositiveButton("Xoá", (dialog, which) -> {
                        String token = new com.example.jobhunter.utils.SessionManager(JobManageActivity.this).getAuthToken();
                        jobViewModel.deleteJob(job.getId(), token);
                    })
                    .setNegativeButton("Huỷ", null)
                    .show();
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

        // Quan sát kết quả xoá job
        jobViewModel.getDeleteJobResultLiveData().observe(this, success -> {
            if (success != null && success) {
                android.widget.Toast.makeText(this, "Xoá thành công!", android.widget.Toast.LENGTH_SHORT).show();
                // jobViewModel.fetchJobs(token); // Đã gọi trong ViewModel
            } else if (success != null) {
                android.widget.Toast.makeText(this, "Xoá thất bại!", android.widget.Toast.LENGTH_SHORT).show();
            }
        });

        // Quan sát lỗi khi xoá job
        jobViewModel.getDeleteJobErrorLiveData().observe(this, errorMsg -> {
            if (errorMsg != null && !errorMsg.isEmpty()) {
                new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Không thể xoá công việc")
                    .setMessage(errorMsg)
                    .setPositiveButton("Đóng", null)
                    .show();
            }
        });
    }

}
