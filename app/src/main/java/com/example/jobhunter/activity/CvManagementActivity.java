package com.example.jobhunter.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jobhunter.R;
import com.example.jobhunter.ViewModel.ResumeViewModel;
import com.example.jobhunter.adapter.ResumeAdapter;
import com.example.jobhunter.model.Resume;
import com.example.jobhunter.util.SharedPrefHelper;

import java.util.ArrayList;
import java.util.List;

public class CvManagementActivity extends AppCompatActivity {

    private static final String TAG = "CvManagementActivity";
    private ResumeViewModel resumeViewModel;
    private ResumeAdapter resumeAdapter;
    private RecyclerView resumesRecyclerView;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cv_management);

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Quản lý CV");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // UI Elements
        progressBar = findViewById(R.id.progressBar);
        resumesRecyclerView = findViewById(R.id.resumesRecyclerView);
        resumesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        resumeAdapter = new ResumeAdapter(this, new ArrayList<>(), false, null);
        resumesRecyclerView.setAdapter(resumeAdapter);

        // ViewModel
        resumeViewModel = new ViewModelProvider(this).get(ResumeViewModel.class);
        observeViewModel();

        // Load data
        loadResumes();
    }

    private void observeViewModel() {
        resumeViewModel.getResumesLiveData().observe(this, resumes -> {
            progressBar.setVisibility(View.GONE);
            if (resumes != null && !resumes.isEmpty()) {
                resumeAdapter.setData(resumes);
            } else {
                Toast.makeText(this, "Không có CV nào được nộp", Toast.LENGTH_SHORT).show();
            }
        });

        resumeViewModel.getErrorLiveData().observe(this, error -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, ""+ error, Toast.LENGTH_LONG).show();
            Log.e(TAG, "Error fetching resumes: " + error);
        });
    }

    private void loadResumes() {
        String token = SharedPrefHelper.getToken(this);
        if (token != null && !token.isEmpty()) {
            progressBar.setVisibility(View.VISIBLE);
            resumeViewModel.getResumesByUser(token);  // Dùng token
        } else {
            Toast.makeText(this, "Bạn cần đăng nhập để xem CV", Toast.LENGTH_SHORT).show();
        }
    }
}
