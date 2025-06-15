package com.example.jobhunter.activity;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.jobhunter.R;
import com.example.jobhunter.adapter.ResumeAdapter;
import com.example.jobhunter.model.Resume;
import com.example.jobhunter.utils.SessionManager;
import com.example.jobhunter.util.constant.ResumeStateEnum;
import com.example.jobhunter.ViewModel.ResumeViewModel;
import java.util.ArrayList;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

public class ResumeManageActivity extends AppCompatActivity {
    private RecyclerView rvResumes;
    private ResumeAdapter adapter;
    private ResumeViewModel resumeViewModel;
    private boolean isAdmin;
    private ProgressBar progressBar;
    private Toolbar toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cv_management);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Quản lý đơn ứng tuyển");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        progressBar = findViewById(R.id.progressBar);
        rvResumes = findViewById(R.id.resumesRecyclerView);
        rvResumes.setLayoutManager(new LinearLayoutManager(this));
        isAdmin = "SUPER_ADMIN".equals(new SessionManager(this).getUserRole());
        adapter = new ResumeAdapter(this, new ArrayList<>(), isAdmin, (resume, newState) -> {
            if (isAdmin) {
                String token = new SessionManager(this).getAuthToken();
                resumeViewModel.updateResumeState(resume.getId(), newState, token);
            }
        });
        rvResumes.setAdapter(adapter);

        resumeViewModel = new ViewModelProvider(this).get(ResumeViewModel.class);
        String token = new SessionManager(this).getAuthToken();
        progressBar.setVisibility(View.VISIBLE);
        resumeViewModel.fetchAllResumes(token);

        resumeViewModel.getResumesLiveData().observe(this, resumes -> {
            progressBar.setVisibility(View.GONE);
            adapter.setData(resumes);
        });
        resumeViewModel.getUpdateResultLiveData().observe(this, success -> {
            if (success != null && success) {
                resumeViewModel.fetchAllResumes(token);
            }
        });
        resumeViewModel.getErrorLiveData().observe(this, error -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, ""+ error, Toast.LENGTH_LONG).show();
        });
    }
} 