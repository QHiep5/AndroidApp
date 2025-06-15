package com.example.jobhunter.activity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jobhunter.R;
import com.example.jobhunter.ViewModel.CompanyViewModel;
import com.example.jobhunter.adapter.CompanyManageAdapter;
import com.example.jobhunter.fragment.AddEditCompanyAdminFragment;
import com.example.jobhunter.model.Company;

import java.util.ArrayList;

public class CompanyManageAdminActivity extends AppCompatActivity {
    private CompanyViewModel companyViewModel;
    private CompanyManageAdapter adapter;
    private RecyclerView rvCompanies;
    private EditText etSearch;
    private Button btnAddCompany;
    private ImageView btnBack;
    private ImageButton btnPrev, btnNext;
    private TextView tvPageInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.company_manage_admin_layout);

        init();
        setupObservers();
        // TODO: Replace with actual token
        companyViewModel.fetchCompanies("");
    }

    private void init() {
        rvCompanies = findViewById(R.id.rv_jobs);
        etSearch = findViewById(R.id.et_search_company);
        btnAddCompany = findViewById(R.id.btn_add_company);
        btnBack = findViewById(R.id.btn_back);
        btnPrev = findViewById(R.id.btn_prev);
        btnNext = findViewById(R.id.btn_next);
        tvPageInfo = findViewById(R.id.tv_page_info);

        btnBack.setOnClickListener(v -> finish());

        // Setup pagination buttons
        btnPrev.setOnClickListener(v -> {
            Integer currentPage = companyViewModel.getCurrentPage().getValue();
            if (currentPage != null && currentPage > 1) {
                companyViewModel.fetchCompanies("", currentPage - 1);
            }
        });

        btnNext.setOnClickListener(v -> {
            Integer currentPage = companyViewModel.getCurrentPage().getValue();
            Integer totalPages = companyViewModel.getTotalPages().getValue();
            if (currentPage != null && totalPages != null && currentPage < totalPages) {
                companyViewModel.fetchCompanies("", currentPage + 1);
            }
        });

        // Add scroll listener to RecyclerView
        rvCompanies.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
                            Animation fadeIn = AnimationUtils.loadAnimation(CompanyManageAdminActivity.this, R.anim.fade_in);
                            paginationLayout.startAnimation(fadeIn);
                        }
                    } else {
                        // Hide pagination bar with animation
                        View paginationLayout = findViewById(R.id.pagination_layout);
                        if (paginationLayout.getVisibility() == View.VISIBLE) {
                            Animation fadeOut = AnimationUtils.loadAnimation(CompanyManageAdminActivity.this, R.anim.fade_out);
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

        companyViewModel = new CompanyViewModel(getApplication());

        adapter = new CompanyManageAdapter(new ArrayList<>(), new CompanyManageAdapter.OnActionClickListener() {
            @Override
            public void onEdit(Company company) {
                showCompanyDialog(company,"Chỉnh sửa công ty",false);
            }
            @Override
            public void onDelete(Company company) {
                showDeleteConfirmationDialog(company);
            }
        });

        rvCompanies.setLayoutManager(new LinearLayoutManager(this));
        rvCompanies.setAdapter(adapter);

        btnAddCompany.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCompanyDialog(null,"Thêm mới công ty",true);
            }
        });
    }

    private void setupObservers() {
        companyViewModel.getCompaniesLiveData().observe(this, companies -> {
            adapter.setData(companies);
        });

        companyViewModel.getErrorLiveData().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });

        companyViewModel.getDeleteResult().observe(this, success -> {
            if (success) {
                Toast.makeText(this, "Xóa công ty thành công", Toast.LENGTH_SHORT).show();
            }
        });

        companyViewModel.getDeleteError().observe(this, error -> {
            if (error != null) {
                showErrorDialog(error);
            }
        });

        // Observe pagination data
        companyViewModel.getCurrentPage().observe(this, page -> updatePaginationUI());
        companyViewModel.getTotalPages().observe(this, pages -> updatePaginationUI());
    }

    private void updatePaginationUI() {
        Integer currentPage = companyViewModel.getCurrentPage().getValue();
        Integer totalPages = companyViewModel.getTotalPages().getValue();

        if (currentPage != null && totalPages != null) {
            tvPageInfo.setText(String.format("Page %d of %d", currentPage, totalPages));
            btnPrev.setEnabled(currentPage > 1);
            btnNext.setEnabled(currentPage < totalPages);
        }
    }

    private void showErrorDialog(String message) {
        new AlertDialog.Builder(this)
            .setTitle("Lỗi")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show();
    }

    private void showDeleteConfirmationDialog(Company company) {
        new AlertDialog.Builder(this)
            .setTitle("Xác nhận xóa")
            .setMessage("Bạn có chắc chắn muốn xóa công ty " + company.getName() + "?")
            .setPositiveButton("Có", (dialog, which) -> {
                // TODO: Replace with actual token
                companyViewModel.deleteCompany(company.getId(), "");
            })
            .setNegativeButton("Không", null)
            .show();
    }

    private void showCompanyDialog(Company company,String tittle,boolean type) {
        FragmentManager fm = getSupportFragmentManager();
        AddEditCompanyAdminFragment dialog = AddEditCompanyAdminFragment.newInstance(company,tittle,type);
        dialog.show(fm, "AddCompanyFragment");
    }
}
