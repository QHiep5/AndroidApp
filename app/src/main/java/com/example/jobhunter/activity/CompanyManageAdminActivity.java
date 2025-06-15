package com.example.jobhunter.activity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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

        btnBack.setOnClickListener(v -> finish());

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
