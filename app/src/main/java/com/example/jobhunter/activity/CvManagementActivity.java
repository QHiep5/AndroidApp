package com.example.jobhunter.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
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
    private Button btnPrev, btnNext;
    private TextView tvPageInfo;
    private View paginationLayout;

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
        btnPrev = findViewById(R.id.btn_prev);
        btnNext = findViewById(R.id.btn_next);
        tvPageInfo = findViewById(R.id.tv_page_info);
        paginationLayout = findViewById(R.id.pagination_layout);

        resumesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        resumeAdapter = new ResumeAdapter(this, new ArrayList<>(), false, null);
        resumesRecyclerView.setAdapter(resumeAdapter);

        // Add scroll listener to RecyclerView
        resumesRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null) {
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
                    if (firstVisibleItemPosition > 0) {
                        // Show pagination bar with animation
                        View paginationLayout = findViewById(R.id.pagination_layout);
                        if (paginationLayout.getVisibility() != View.VISIBLE) {
                            paginationLayout.setVisibility(View.VISIBLE);
                            Animation fadeIn = AnimationUtils.loadAnimation(CvManagementActivity.this, R.anim.fade_in);
                            paginationLayout.startAnimation(fadeIn);
                        }
                    } else {
                        // Hide pagination bar with animation
                        View paginationLayout = findViewById(R.id.pagination_layout);
                        if (paginationLayout.getVisibility() == View.VISIBLE) {
                            Animation fadeOut = AnimationUtils.loadAnimation(CvManagementActivity.this, R.anim.fade_out);
                            fadeOut.setAnimationListener(new Animation.AnimationListener() {
                                @Override
                                public void onAnimationStart(Animation animation) {}

                                @Override
                                public void onAnimationEnd(Animation animation) {
                                    paginationLayout.setVisibility(View.GONE);
                                }

                                @Override
                                public void onAnimationRepeat(Animation animation) {}
                            });
                            paginationLayout.startAnimation(fadeOut);
                        }
                    }
                }
            }
        });

        // Setup pagination buttons
        btnPrev.setOnClickListener(v -> {
            resumeViewModel.previousPage();
            loadResumes();
        });

        btnNext.setOnClickListener(v -> {
            resumeViewModel.nextPage();
            loadResumes();
        });

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
                // Show pagination when we have data
                if (paginationLayout.getVisibility() != View.VISIBLE) {
                    paginationLayout.setVisibility(View.VISIBLE);
                    Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
                    fadeIn.setDuration(300);
                    paginationLayout.startAnimation(fadeIn);
                }
            } else {
                resumeAdapter.setData(new ArrayList<>());
                Toast.makeText(this, "Không có CV nào được nộp", Toast.LENGTH_SHORT).show();
                // Hide pagination when no data
                if (paginationLayout.getVisibility() == View.VISIBLE) {
                    Animation fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);
                    fadeOut.setDuration(300);
                    fadeOut.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {}

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            paginationLayout.setVisibility(View.GONE);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {}
                    });
                    paginationLayout.startAnimation(fadeOut);
                }
            }
        });

        resumeViewModel.getErrorLiveData().observe(this, error -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, ""+ error, Toast.LENGTH_LONG).show();
            Log.e(TAG, "Error fetching resumes: " + error);
            // Hide pagination on error
            if (paginationLayout.getVisibility() == View.VISIBLE) {
                paginationLayout.setVisibility(View.GONE);
            }
        });

        resumeViewModel.getCurrentPageLiveData().observe(this, currentPage -> {
            updatePaginationUI();
        });

        resumeViewModel.getTotalPagesLiveData().observe(this, totalPages -> {
            updatePaginationUI();
        });
    }

    private void updatePaginationUI() {
        Integer currentPage = resumeViewModel.getCurrentPageLiveData().getValue();
        Integer totalPages = resumeViewModel.getTotalPagesLiveData().getValue();
        
        if (currentPage != null && totalPages != null) {
            tvPageInfo.setText(String.format("Page %d/%d", currentPage, totalPages));
            btnPrev.setEnabled(currentPage > 1);
            btnNext.setEnabled(currentPage < totalPages);
            
            // Add visual feedback for disabled buttons
            btnPrev.setAlpha(btnPrev.isEnabled() ? 1.0f : 0.5f);
            btnNext.setAlpha(btnNext.isEnabled() ? 1.0f : 0.5f);
        }
    }

    private void loadResumes() {
        String token = SharedPrefHelper.getToken(this);
        if (token != null && !token.isEmpty()) {
            progressBar.setVisibility(View.VISIBLE);
            // Reset pagination visibility when loading new data
            paginationLayout.setVisibility(View.GONE);
            resumeViewModel.getResumesByUser(token);
        } else {
            Toast.makeText(this, "Bạn cần đăng nhập để xem CV", Toast.LENGTH_SHORT).show();
        }
    }
}
