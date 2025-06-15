package com.example.jobhunter.activity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.jobhunter.R;
import com.example.jobhunter.ViewModel.CompanyViewModel;
import com.example.jobhunter.ViewModel.JobViewModel;
import com.example.jobhunter.ViewModel.SkillViewModel;
import com.example.jobhunter.model.Company;
import com.example.jobhunter.model.Job;
import com.example.jobhunter.model.Skill;
import com.example.jobhunter.utils.SessionManager;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import com.example.jobhunter.util.constant.LevelEnum;
import com.example.jobhunter.ViewModel.JobDetailViewModel;
import android.text.Html;
import java.text.DecimalFormat;

public class UpdateJobActivity extends AppCompatActivity {
    private static final String TAG = "UpdateJobActivity";
    private ImageView btnBack;
    private EditText etJobName, etSalary, etQuantity, etDescription;
    private TextView tvSelectSkills;
    private ChipGroup chipGroupSkills;
    private Spinner spinnerLevel, spinnerCompany, spinnerLocation;
    private EditText etStartDate, etEndDate;
    private Switch switchStatus;
    private Button btnUpdateJob, btnCancel;

    private JobDetailViewModel jobDetailViewModel;
    private JobViewModel jobViewModel;
    private SkillViewModel skillViewModel;
    private CompanyViewModel companyViewModel;
    private SessionManager sessionManager;

    private List<Skill> allSkillsList = new ArrayList<>();
    private boolean[] selectedSkillsFlags;
    private ArrayList<String> selectedSkillNames = new ArrayList<>();
    private List<Company> allCompaniesList = new ArrayList<>();
    private Map<String, String> locationMap = new LinkedHashMap<>();
    private Job currentJob;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_job);

        sessionManager = new SessionManager(this);
        btnBack = findViewById(R.id.btn_back);
        etJobName = findViewById(R.id.et_job_name);
        tvSelectSkills = findViewById(R.id.tv_select_skills);
        chipGroupSkills = findViewById(R.id.chip_group_skills);
        spinnerLocation = findViewById(R.id.spinner_location);
        etSalary = findViewById(R.id.et_salary);
        etQuantity = findViewById(R.id.et_quantity);
        spinnerLevel = findViewById(R.id.spinner_level);
        spinnerCompany = findViewById(R.id.spinner_company);
        etStartDate = findViewById(R.id.et_start_date);
        etEndDate = findViewById(R.id.et_end_date);
        etDescription = findViewById(R.id.et_description);
        switchStatus = findViewById(R.id.switch_status);
        btnUpdateJob = findViewById(R.id.btn_update_job);
        btnCancel = findViewById(R.id.btn_cancel);

        jobDetailViewModel = new ViewModelProvider(this).get(JobDetailViewModel.class);
        jobViewModel = new ViewModelProvider(this).get(JobViewModel.class);
        skillViewModel = new ViewModelProvider(this).get(SkillViewModel.class);
        companyViewModel = new ViewModelProvider(this).get(CompanyViewModel.class);

        loadSkills();
        loadCompanies();
        setupLevelSpinner();
        setupLocationSpinner();

        btnBack.setOnClickListener(v -> onBackPressed());
        etStartDate.setOnClickListener(v -> showDatePickerDialog(etStartDate));
        etEndDate.setOnClickListener(v -> showDatePickerDialog(etEndDate));
        tvSelectSkills.setOnClickListener(v -> showSkillSelectionDialog());
        btnUpdateJob.setOnClickListener(v -> updateJob());
        btnCancel.setOnClickListener(v -> finish());

        jobViewModel.getUpdateJobResultLiveData().observe(this, success -> {
            if (success != null && success) {
                Toast.makeText(UpdateJobActivity.this, "Cập nhật công việc thành công!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(UpdateJobActivity.this, "Cập nhật công việc thất bại. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
            }
        });
        jobViewModel.getErrorLiveData().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(UpdateJobActivity.this, "Lỗi: " + error, Toast.LENGTH_LONG).show();
            }
        });

        // Nhận JOB_ID từ Intent
        long jobId = getIntent().getLongExtra("JOB_ID", -1);
        if (jobId == -1) {
            Toast.makeText(this, "Không tìm thấy ID công việc!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        // Sử dụng JobViewModel để lấy Job đầy đủ trường cho update
        String token = sessionManager.getAuthToken();
        jobViewModel.getJobForUpdateLiveData().observe(this, job -> {
            if (job != null) {
                currentJob = job;
                fillJobData(currentJob);
            }
        });
        jobViewModel.fetchJobByIdForUpdate(getApplication(), String.valueOf(jobId), token);
    }

    private void fillJobData(Job job) {
        etJobName.setText(job.getName());

        // Hiển thị mức lương đúng định dạng
        DecimalFormat salaryFormat = new DecimalFormat("#.##");
        etSalary.setText(job.getSalary() > 0 ? salaryFormat.format(job.getSalary()) : "");

        etQuantity.setText(String.valueOf(job.getQuantity()));

        // Loại bỏ HTML khỏi mô tả công việc
        if (job.getDescription() != null) {
            String plainDesc = Html.fromHtml(job.getDescription(), Html.FROM_HTML_MODE_LEGACY).toString().trim();
            etDescription.setText(plainDesc);
        } else {
            etDescription.setText("");
        }

        switchStatus.setChecked(job.isActive());

        // Ngày bắt đầu/kết thúc
        etStartDate.setText((job.getStartDate() != null && job.getStartDate().length() >= 10) ? job.getStartDate().substring(0, 10) : "");
        etEndDate.setText((job.getEndDate() != null && job.getEndDate().length() >= 10) ? job.getEndDate().substring(0, 10) : "");

        // Set Spinner Location
        if (job.getLocation() != null && locationMap != null) {
            int pos = 0;
            for (int i = 0; i < spinnerLocation.getCount(); i++) {
                String display = spinnerLocation.getItemAtPosition(i).toString();
                if (locationMap.get(display) != null && locationMap.get(display).equals(job.getLocation())) {
                    pos = i;
                    break;
                }
            }
            spinnerLocation.setSelection(pos);
        }

        // Set Spinner Level
        if (job.getLevel() != null) {
            String levelStr = job.getLevel().name().substring(0, 1) + job.getLevel().name().substring(1).toLowerCase();
            for (int i = 0; i < spinnerLevel.getCount(); i++) {
                if (spinnerLevel.getItemAtPosition(i).toString().equalsIgnoreCase(levelStr)) {
                    spinnerLevel.setSelection(i);
                    break;
                }
            }
        }

        // Set Spinner Company (hiện đúng tên công ty)
        if (job.getCompany() != null && allCompaniesList != null && allCompaniesList.size() > 0) {
            boolean found = false;
            long jobCompanyId = job.getCompany().getId();
            for (int i = 0; i < allCompaniesList.size(); i++) {
                long listCompanyId = allCompaniesList.get(i).getId();
                if (listCompanyId == jobCompanyId) {
                    spinnerCompany.setSelection(i + 1); // +1 vì 0 là hint
                    found = true;
                    break;
                }
            }
            if (!found) {
                // Nếu không tìm thấy, log ra để debug
                android.util.Log.w("UpdateJobActivity", "Không tìm thấy công ty với ID: " + jobCompanyId + ". Danh sách: " + allCompaniesList);
                spinnerCompany.setSelection(0); // Chọn hint
            }
        }

        // Set Skills (ChipGroup)
        if (job.getSkills() != null && allSkillsList != null && selectedSkillsFlags != null) {
            chipGroupSkills.removeAllViews();
            for (int i = 0; i < allSkillsList.size(); i++) {
                Skill skill = allSkillsList.get(i);
                boolean selected = false;
                for (Skill jobSkill : job.getSkills()) {
                    if (jobSkill.getId() == skill.getId()) {
                        selected = true;
                        break;
                    }
                }
                selectedSkillsFlags[i] = selected;
                if (selected) {
                    addChipToChipGroup(skill.getName());
                    if (!selectedSkillNames.contains(skill.getName()))
                        selectedSkillNames.add(skill.getName());
                }
            }
        }
    }

    private void loadSkills() {
        skillViewModel.getSkillsLiveData().observe(this, skills -> {
            if (skills != null) {
                allSkillsList.clear();
                allSkillsList.addAll(skills);
                selectedSkillsFlags = new boolean[allSkillsList.size()];
            }
        });
        skillViewModel.fetchSkills();
    }
    private void loadCompanies() {
        companyViewModel.getCompaniesLiveData().observe(this, companies -> {
            if (companies != null) {
                allCompaniesList.clear();
                allCompaniesList.addAll(companies);
                List<String> companyNames = new ArrayList<>();
                companyNames.add("Hãy chọn Công Ty"); // Thêm hint vào đầu danh sách
                for (Company company : companies) {
                    companyNames.add(company.getName());
                }
                ArrayAdapter<String> companyAdapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_item, companyNames);
                companyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCompany.setAdapter(companyAdapter);

                // Nếu đã có currentJob, gọi lại fillJobData để setSelection đúng tên công ty
                if (currentJob != null) {
                    fillJobData(currentJob);
                }
            }
        });
        String token = sessionManager.getAuthToken();
        companyViewModel.fetchCompanies(token);
    }
    private void setupLevelSpinner() {
        List<String> levelsDisplay = new ArrayList<>(Arrays.asList("Hãy chọn Trình độ", "Intern", "Fresher", "Junior", "Middle", "Senior"));
        ArrayAdapter<String> levelAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, levelsDisplay);
        levelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLevel.setAdapter(levelAdapter);
    }
    private void setupLocationSpinner() {
        locationMap.put("Hãy chọn Địa điểm", "");
        locationMap.put("Hồ Chí Minh", "HOCHIMINH");
        locationMap.put("Hà Nội", "HANOI");
        locationMap.put("Đà Nẵng", "DANANG");
        locationMap.put("Khác", "OTHER");
        List<String> locationsDisplay = new ArrayList<>(locationMap.keySet());
        ArrayAdapter<String> locationAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, locationsDisplay);
        locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLocation.setAdapter(locationAdapter);
    }
    private void showDatePickerDialog(final EditText targetEditText) {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year1, monthOfYear, dayOfMonth);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    targetEditText.setText(sdf.format(selectedDate.getTime()));
                }, year, month, day);
        datePickerDialog.show();
    }
    private void showSkillSelectionDialog() {
        if (allSkillsList.isEmpty()) {
            Toast.makeText(this, "Đang tải kỹ năng, vui lòng thử lại sau.", Toast.LENGTH_SHORT).show();
            return;
        }
        String[] skillNamesArray = new String[allSkillsList.size()];
        for (int i = 0; i < allSkillsList.size(); i++) {
            skillNamesArray[i] = allSkillsList.get(i).getName();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn Kỹ Năng");
        builder.setMultiChoiceItems(skillNamesArray, selectedSkillsFlags, (dialog, which, isChecked) -> {
            selectedSkillsFlags[which] = isChecked;
        });
        builder.setPositiveButton("OK", (dialog, which) -> {
            chipGroupSkills.removeAllViews();
            selectedSkillNames.clear();
            for (int i = 0; i < allSkillsList.size(); i++) {
                if (selectedSkillsFlags[i]) {
                    Skill selectedSkill = allSkillsList.get(i);
                    selectedSkillNames.add(selectedSkill.getName());
                    addChipToChipGroup(selectedSkill.getName());
                }
            }
        });
        builder.setNegativeButton("Hủy", null);
        builder.create().show();
    }
    private void addChipToChipGroup(String skillName) {
        Chip chip = new Chip(this);
        chip.setText(skillName);
        chip.setCloseIconVisible(true);
        chip.setOnCloseIconClickListener(v -> {
            chipGroupSkills.removeView(chip);
            selectedSkillNames.remove(skillName);
            for (int i = 0; i < allSkillsList.size(); i++) {
                if (allSkillsList.get(i).getName().equals(skillName)) {
                    selectedSkillsFlags[i] = false;
                    break;
                }
            }
        });
        chipGroupSkills.addView(chip);
    }
    private void updateJob() {
        // Lấy dữ liệu từ các trường nhập liệu
        String name = etJobName.getText().toString().trim();
        String locationDisplay = spinnerLocation.getSelectedItem().toString();
        String location = locationMap.get(locationDisplay);
        String salaryStr = etSalary.getText().toString().trim();
        String quantityStr = etQuantity.getText().toString().trim();
        String levelDisplay = spinnerLevel.getSelectedItem().toString();
        String description = etDescription.getText().toString().trim();
        String startDate = etStartDate.getText().toString().trim();
        String endDate = etEndDate.getText().toString().trim();
        boolean isActive = switchStatus.isChecked();
        int companyPosition = spinnerCompany.getSelectedItemPosition();

        // Validate
        if (name.isEmpty() || location == null || location.isEmpty() || salaryStr.isEmpty() || quantityStr.isEmpty()
                || levelDisplay.equals("Hãy chọn Trình độ") || companyPosition == 0 || startDate.isEmpty() || endDate.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin bắt buộc.", Toast.LENGTH_LONG).show();
            return;
        }
        double salary;
        int quantity;
        try {
            salary = Double.parseDouble(salaryStr);
            quantity = Integer.parseInt(quantityStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Mức lương và Số lượng phải là số hợp lệ.", Toast.LENGTH_LONG).show();
            return;
        }
        // Lấy trình độ
        String levelApiValue = levelDisplay.toUpperCase();
        // Lấy công ty
        Company selectedCompany = allCompaniesList.get(companyPosition - 1); // vì 0 là hint
        // Lấy skills
        List<Skill> selectedSkills = new ArrayList<>();
        for (int i = 0; i < allSkillsList.size(); i++) {
            if (selectedSkillsFlags[i]) {
                selectedSkills.add(allSkillsList.get(i));
            }
        }
        // Chuyển đổi ngày tháng sang định dạng ISO 8601 (yyyy-MM-dd'T'HH:mm:ss.SSS'Z')
        SimpleDateFormat sdfDateOnly = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat isoDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        isoDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        String startDateIso = startDate;
        String endDateIso = endDate;
        try {
            startDateIso = isoDateFormat.format(sdfDateOnly.parse(startDate));
            endDateIso = isoDateFormat.format(sdfDateOnly.parse(endDate));
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi định dạng ngày tháng.", Toast.LENGTH_LONG).show();
            return;
        }
        // Build Job object
        Job job = new Job();
        job.setId(currentJob.getId());
        job.setName(name);
        job.setLocation(location);
        job.setSalary(salary);
        job.setQuantity(quantity);
        job.setLevel(com.example.jobhunter.util.constant.LevelEnum.valueOf(levelApiValue));
        job.setDescription(description);
        job.setStartDate(startDateIso);
        job.setEndDate(endDateIso);
        job.setActive(isActive);
        job.setCompany(selectedCompany);
        job.setSkills(selectedSkills);
        // Gọi update
        String token = sessionManager.getAuthToken();
        jobViewModel.updateJob(job, token);
    }
} 