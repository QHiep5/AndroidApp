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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class ResumeManageActivity extends AppCompatActivity {
    private RecyclerView rvResumes;
    private ResumeAdapter adapter;
    private ResumeViewModel resumeViewModel;
    private boolean isAdmin;
    private ProgressBar progressBar;
    private Toolbar toolbar;
    private ImageButton btnPrev, btnNext;
    private TextView tvPageInfo;
    private View paginationLayout;

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

        // Initialize UI components
        progressBar = findViewById(R.id.progressBar);
        rvResumes = findViewById(R.id.resumesRecyclerView);
        btnPrev = findViewById(R.id.btn_prev);
        btnNext = findViewById(R.id.btn_next);
        tvPageInfo = findViewById(R.id.tv_page_info);
        paginationLayout = findViewById(R.id.pagination_layout);

        rvResumes.setLayoutManager(new LinearLayoutManager(this));
        isAdmin = "SUPER_ADMIN".equals(new SessionManager(this).getUserRole());
        adapter = new ResumeAdapter(this, new ArrayList<>(), isAdmin, (resume, newState) -> {
            if (isAdmin) {
                String token = new SessionManager(this).getAuthToken();
                resumeViewModel.updateResumeState(resume.getId(), newState, token);
            }
        });
        rvResumes.setAdapter(adapter);

        // Add scroll listener for pagination visibility
        rvResumes.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null) {
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
                    if (firstVisibleItemPosition > 0) {
                        // Show pagination bar with animation
                        if (paginationLayout.getVisibility() != View.VISIBLE) {
                            paginationLayout.setVisibility(View.VISIBLE);
                            Animation fadeIn = AnimationUtils.loadAnimation(ResumeManageActivity.this, R.anim.fade_in);
                            fadeIn.setDuration(300);
                            paginationLayout.startAnimation(fadeIn);
                        }
                    } else {
                        // Hide pagination bar with animation
                        if (paginationLayout.getVisibility() == View.VISIBLE) {
                            Animation fadeOut = AnimationUtils.loadAnimation(ResumeManageActivity.this, R.anim.fade_out);
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

        resumeViewModel = new ViewModelProvider(this).get(ResumeViewModel.class);
        observeViewModel();
        loadResumes();
    }

    private void observeViewModel() {
        resumeViewModel.getResumesLiveData().observe(this, resumes -> {
            progressBar.setVisibility(View.GONE);
            if (resumes != null && !resumes.isEmpty()) {
                adapter.setData(resumes);
                // Show pagination when we have data
                if (paginationLayout.getVisibility() != View.VISIBLE) {
                    paginationLayout.setVisibility(View.VISIBLE);
                    Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
                    fadeIn.setDuration(300);
                    paginationLayout.startAnimation(fadeIn);
                }
            } else {
                adapter.setData(new ArrayList<>());
                Toast.makeText(this, "Không có đơn ứng tuyển nào", Toast.LENGTH_SHORT).show();
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

        resumeViewModel.getUpdateResultLiveData().observe(this, success -> {
            if (success != null && success) {
                loadResumes();
            }
        });

        resumeViewModel.getErrorLiveData().observe(this, error -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, ""+ error, Toast.LENGTH_LONG).show();
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
            tvPageInfo.setText(String.format("Trang %d/%d", currentPage, totalPages));
            btnPrev.setEnabled(currentPage > 1);
            btnNext.setEnabled(currentPage < totalPages);
            
            // Add visual feedback for disabled buttons
            btnPrev.setAlpha(btnPrev.isEnabled() ? 1.0f : 0.5f);
            btnNext.setAlpha(btnNext.isEnabled() ? 1.0f : 0.5f);
        }
    }

    private void loadResumes() {
        String token = new SessionManager(this).getAuthToken();
        if (token != null && !token.isEmpty()) {
            progressBar.setVisibility(View.VISIBLE);
            // Reset pagination visibility when loading new data
            paginationLayout.setVisibility(View.GONE);
            resumeViewModel.fetchAllResumes(token);
        } else {
            Toast.makeText(this, "Bạn cần đăng nhập để xem đơn ứng tuyển", Toast.LENGTH_SHORT).show();
        }
    }
} 