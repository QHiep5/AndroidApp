package com.example.jobhunter.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.jobhunter.R;
import com.example.jobhunter.ViewModel.JobDetailViewModel;
import com.example.jobhunter.api.ApiConfig;
import com.example.jobhunter.model.Job;
import com.example.jobhunter.model.Skill;
import com.example.jobhunter.utils.SessionManager;
import com.squareup.picasso.Picasso;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.stream.Collectors;

public class JobDetailActivity extends AppCompatActivity {

    private static final String TAG = "JobDetailActivity";
    private JobDetailViewModel jobDetailViewModel;
    private ImageView btnBack, ivCompanyLogo;
    private TextView tvJobTitleData, tvJobLocationData, tvJobSalaryData, tvJobSkillsData, tvJobDescriptionData,
            tvCompanyName;
    private Button btnApply;
    private long jobId;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_detail);

        sessionManager = new SessionManager(this);

        jobId = getIntent().getLongExtra("JOB_ID", -1);
        if (jobId == -1) {
            Toast.makeText(this, "Lỗi: không tìm thấy ID công việc.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        findViews();
        setupToolbar();
        setupViewModel();

        jobDetailViewModel.fetchJobById(getApplication(), String.valueOf(jobId), null);
    }

    private void findViews() {
        btnBack = findViewById(R.id.btn_back);
        tvJobTitleData = findViewById(R.id.tv_job_title_data);
        tvJobLocationData = findViewById(R.id.tv_job_location_data);
        tvJobSalaryData = findViewById(R.id.tv_job_salary_data);
        tvJobSkillsData = findViewById(R.id.tv_job_skills_data);
        tvJobDescriptionData = findViewById(R.id.tv_job_description_data);
        tvCompanyName = findViewById(R.id.tv_company_name);
        ivCompanyLogo = findViewById(R.id.iv_company_logo);
        btnApply = findViewById(R.id.btn_apply);

        btnApply.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("jobhunter_prefs", MODE_PRIVATE);
            long userId = prefs.getInt("user_id", 0);
            String userEmail = prefs.getString("user_email", "");
            if (userId == 0 || userEmail.isEmpty()) {
                Toast.makeText(this, "Bạn cần đăng nhập để nộp CV!", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, UploadCVActivity.class);
            intent.putExtra("userId", userId);
            intent.putExtra("jobId", jobId);
            intent.putExtra("userEmail", userEmail);
            startActivity(intent);
        });
    }

    private void setupToolbar() {
        btnBack.setOnClickListener(v -> finish());
    }

    private void setupViewModel() {
        jobDetailViewModel = new ViewModelProvider(this).get(JobDetailViewModel.class);

        jobDetailViewModel.getJobLiveData().observe(this, job -> {
            Log.d(TAG, "LiveData observer triggered. Updating UI.");
            if (job != null) {
                updateUi(job);
            } else {
                Log.w(TAG, "Job object is null, UI not updated.");
            }
        });

        jobDetailViewModel.getErrorLiveData().observe(this, error -> {
            Log.e(TAG, "Error LiveData observer triggered: " + error);
            Toast.makeText(this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
        });
    }

    private void updateUi(Job job) {
        Log.d(TAG, "updateUi called with job: " + job.getName());
        tvJobTitleData.setText(job.getName());
        tvJobLocationData.setText(job.getLocation());
        tvJobDescriptionData.setText(Html.fromHtml(job.getDescription()));
        
        if (job.getSkills() != null && !job.getSkills().isEmpty()) {
            String skillsString = job.getSkills().stream()
                                     .map(Skill::getName)
                                     .collect(Collectors.joining(", "));
            tvJobSkillsData.setText(skillsString);
        } else {
            tvJobSkillsData.setText("Không yêu cầu kỹ năng cụ thể");
        }

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        tvJobSalaryData.setText(currencyFormat.format(job.getSalary()));

        if (job.getCompany() != null) {
            tvCompanyName.setText(job.getCompany().getName());
            String logoFileName = job.getCompany().getLogo();
            if (logoFileName != null && !logoFileName.isEmpty()) {
                String logoUrl = ApiConfig.LOGO_BASE_URL + logoFileName;
                Picasso.get()
                        .load(logoUrl)
                        .placeholder(R.drawable.ic_company)
                        .error(R.drawable.ic_company)
                        .into(ivCompanyLogo);
            } else {
                ivCompanyLogo.setImageResource(R.drawable.ic_company);
            }
        }
    }
}
