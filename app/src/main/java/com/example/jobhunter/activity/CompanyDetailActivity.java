package com.example.jobhunter.activity;

import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.jobhunter.R;
import com.example.jobhunter.ViewModel.CompanyDetailViewModel;
import com.example.jobhunter.model.Company;
import com.squareup.picasso.Picasso;

public class CompanyDetailActivity extends AppCompatActivity {
    private CompanyDetailViewModel viewModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.company_detail);

        long companyId = getIntent().getLongExtra("company_id", -1);
        Log.d("CompanyDetailActivity", "Company ID: " + companyId);
        String token = ""; // Lấy token từ SharedPreferences nếu có

        // Bind view
        ImageView imgLogo = findViewById(R.id.img_company_logo);
        TextView tvName = findViewById(R.id.tv_company_name);
        TextView tvBadge = findViewById(R.id.tv_company_badge);
        TextView tvField = findViewById(R.id.tv_company_field);
        TextView tvAddress = findViewById(R.id.tv_company_address);
        TextView tvWebsite = findViewById(R.id.tv_company_website);
        TextView tvDescription = findViewById(R.id.tv_company_description);
        TextView tvEmail = findViewById(R.id.tv_company_email);
        TextView tvHotline = findViewById(R.id.tv_company_hotline);
        TextView tvJobs = findViewById(R.id.tv_company_jobs_placeholder);

        tvName.setSelected(true);
        tvBadge.setSelected(true);
        tvField.setSelected(true);
        tvWebsite.setSelected(true);

        viewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication())).get(CompanyDetailViewModel.class);
        viewModel.getCompanyLiveData().observe(this, company -> {
            if (company == null) return;
            // Hiển thị dữ liệu lên giao diện
            String logoUrl = "http://192.168.0.103:8080/storage/company/" + company.getLogo();
            Picasso.get()
                    .load(logoUrl)
                    .placeholder(R.drawable.ic_company)
                    .error(R.drawable.ic_company)
                    .into(imgLogo);
            tvName.setText(company.getName());
            tvBadge.setText("  ★ Top Company");
            tvField.setText("Ngân hàng/ Tài chính"); // Nếu có trường ngành nghề thì lấy từ API
            tvAddress.setText("  • " + company.getAddress());
            tvWebsite.setText("🌐 www.company.com"); // Nếu có trường website thì lấy từ API
            // Chuyển HTML sang text thuần
            String plainDescription = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N ?
                Html.fromHtml(company.getDescription(), Html.FROM_HTML_MODE_LEGACY).toString() :
                Html.fromHtml(company.getDescription()).toString();
            tvDescription.setText(plainDescription);
            tvEmail.setText("Email: info@company.com"); // Nếu có trường email thì lấy từ API
            tvHotline.setText("   Hotline: 1900 1234"); // Nếu có trường hotline thì lấy từ API
            tvJobs.setText("Chức năng hiển thị việc làm sẽ bổ sung sau...");
        });
        viewModel.getErrorLiveData().observe(this, error -> {
            if (error != null) Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
        });
        viewModel.fetchCompany(String.valueOf(companyId), token);
    }
}
