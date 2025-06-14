package com.example.jobhunter.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jobhunter.R;
import com.example.jobhunter.ViewModel.CompanyViewModel;
import com.example.jobhunter.adapter.CompanyManageAdapter;
import com.example.jobhunter.fragment.AddCompanyFragment;
import com.example.jobhunter.model.Company;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class CompanyManageAdminActivity extends AppCompatActivity {
    private CompanyViewModel companyViewModel;
    private CompanyManageAdapter adapter;
    private RecyclerView rvCompanies;
    private EditText etSearch;
    private Button btnAddCompany;

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

        companyViewModel = new CompanyViewModel(getApplication());
        adapter = new CompanyManageAdapter(new ArrayList<>(), new CompanyManageAdapter.OnActionClickListener() {
            @Override
            public void onEdit(Company company) {
                showCompanyDialog(company,"Chỉnh sửa công ty");
            }

            @Override
            public void onDelete(Company company) {
                // TODO: Implement delete functionality
                Toast.makeText(CompanyManageAdminActivity.this, "Delete: " + company.getName(), Toast.LENGTH_SHORT).show();
            }
        });

        rvCompanies.setLayoutManager(new LinearLayoutManager(this));
        rvCompanies.setAdapter(adapter);

        btnAddCompany.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCompanyDialog(null,"Thêm mới công ty");
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
    }

    private void showCompanyDialog(Company company,String tittle) {
        FragmentManager fm = getSupportFragmentManager();
        AddCompanyFragment dialog = AddCompanyFragment.newInstance(company,tittle);
        dialog.show(fm, "AddCompanyFragment");
    }
}
