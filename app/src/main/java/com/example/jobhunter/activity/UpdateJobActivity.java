package com.example.jobhunter.activity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import com.example.jobhunter.util.LoadingDialog;
import com.example.jobhunter.utils.SessionManager;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import com.example.jobhunter.util.constant.LevelEnum;
import com.example.jobhunter.ViewModel.JobDetailViewModel;


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
    private ImageView ivCompanyLogo;

    private JobDetailViewModel jobDetailViewModel;
    private JobViewModel jobViewModel;
    private SkillViewModel skillViewModel;
    private CompanyViewModel companyViewModel;
    private SessionManager sessionManager;
    private LoadingDialog loadingDialog;

    private List<Skill> allSkillsList = new ArrayList<>();
    private boolean[] selectedSkillsFlags;
    private ArrayList<String> selectedSkillNames = new ArrayList<>();
    private List<Company> allCompaniesList = new ArrayList<>();
    private Map<String, String> locationMap = new LinkedHashMap<>();
    private Job currentJob;
    private long jobId;
    private Company selectedCompany;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_job);

        sessionManager = new SessionManager(this);
        initViews();
        initViewModels();
        setupSpinners();
        setupListeners();

        // Lấy jobId từ Intent
        jobId = getIntent().getLongExtra("JOB_ID", -1);
        if (jobId == -1) {
            Toast.makeText(this, "Không tìm thấy thông tin công việc", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Load dữ liệu
        loadData();
    }

    private void initViews() {
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
        loadingDialog = new LoadingDialog(this);
    }

    private void initViewModels() {
        jobViewModel = new ViewModelProvider(this).get(JobViewModel.class);
        skillViewModel = new ViewModelProvider(this).get(SkillViewModel.class);
        companyViewModel = new ViewModelProvider(this).get(CompanyViewModel.class);

        // Observe job details
        jobViewModel.getJobForUpdateLiveData().observe(this, job -> {
            if (job != null) {
                currentJob = job;
                fillJobData(job);
            }
        });

        // Observe companies
        companyViewModel.getCompaniesLiveData().observe(this, companies -> {
            if (companies != null) {
                allCompaniesList.clear();
                allCompaniesList.addAll(companies);
                updateCompanySpinner();
            }
        });

        // Observe skills
        skillViewModel.getSkillsLiveData().observe(this, skills -> {
            if (skills != null) {
                allSkillsList.clear();
                allSkillsList.addAll(skills);
                selectedSkillsFlags = new boolean[allSkillsList.size()];
            }
        });

        // Observe update result
        jobViewModel.getUpdateJobResultLiveData().observe(this, success -> {
            loadingDialog.dismiss();
            if (success != null && success) {
                Toast.makeText(UpdateJobActivity.this, "Cập nhật công việc thành công!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(UpdateJobActivity.this, "Cập nhật công việc thất bại. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
            }
        });

        // Observe error
        jobViewModel.getErrorLiveData().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(UpdateJobActivity.this, "Lỗi: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupSpinners() {
        setupLevelSpinner();
        setupLocationSpinner();
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> onBackPressed());
        etStartDate.setOnClickListener(v -> showDatePickerDialog(etStartDate));
        etEndDate.setOnClickListener(v -> showDatePickerDialog(etEndDate));
        tvSelectSkills.setOnClickListener(v -> showSkillSelectionDialog());
        btnUpdateJob.setOnClickListener(v -> updateJob());
        btnCancel.setOnClickListener(v -> finish());

        spinnerCompany.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0 && allCompaniesList != null && !allCompaniesList.isEmpty()) {
                    selectedCompany = allCompaniesList.get(position - 1);
                } else {
                    selectedCompany = null;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedCompany = null;
            }
        });
    }

    private void loadData() {
        loadingDialog.show("Đang tải dữ liệu...");
        String token = sessionManager.getAuthToken();
        
        // Load job details
        jobViewModel.fetchJobByIdForUpdate(getApplication(), String.valueOf(jobId), token);
        
        // Load companies
        loadCompanies();
        
        // Load skills
        skillViewModel.fetchSkills();
    }

    private void updateCompanySpinner() {
        List<String> companyNames = new ArrayList<>();
        companyNames.add("Hãy chọn Công Ty"); // Hint
        for (Company company : allCompaniesList) {
            companyNames.add(company.getName());
        }
        
        ArrayAdapter<String> companyAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, companyNames);
        companyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCompany.setAdapter(companyAdapter);

        // Set selected company if exists
        if (currentJob != null && currentJob.getCompany() != null) {
            for (int i = 0; i < allCompaniesList.size(); i++) {
                if (allCompaniesList.get(i).getId() == currentJob.getCompany().getId()) {
                    spinnerCompany.setSelection(i + 1); // +1 vì 0 là hint
                    selectedCompany = allCompaniesList.get(i);
                    break;
                }
            }
        }
    }

    private void loadCompanies() {
        loadingDialog.show("Đang tải danh sách công ty...");
        String token = sessionManager.getAuthToken();
        companyViewModel.fetchCompanies(token);
        companyViewModel.getCompaniesLiveData().observe(this, companies -> {
            if (companies != null) {
                allCompaniesList.clear();
                allCompaniesList.addAll(companies);
                List<String> companyNames = new ArrayList<>();
                companyNames.add("Hãy chọn Công Ty"); // Hint
                for (Company company : allCompaniesList) {
                    companyNames.add(company.getName());
                }
                ArrayAdapter<String> companyAdapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_item, companyNames);
                companyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCompany.setAdapter(companyAdapter);

                // Set selected company if exists
                if (currentJob != null && currentJob.getCompany() != null) {
                    for (int i = 0; i < allCompaniesList.size(); i++) {
                        if (allCompaniesList.get(i).getId() == currentJob.getCompany().getId()) {
                            spinnerCompany.setSelection(i + 1); // +1 vì 0 là hint
                            selectedCompany = allCompaniesList.get(i);
                            break;
                        }
                    }
                }
                loadingDialog.dismiss();
            }
        });
    }

    private void fillJobData(Job job) {
        if (job == null) return;
        
        currentJob = job;
        etJobName.setText(job.getName());

        // Format lương thành số có dấu phẩy
        DecimalFormat decimalFormat = new DecimalFormat("#,###");
        String formattedSalary = decimalFormat.format(job.getSalary());
        etSalary.setText(formattedSalary);

        etQuantity.setText(String.valueOf(job.getQuantity()));
        
        // Xóa HTML tags trong mô tả
        String description = job.getDescription();
        if (description != null) {
            description = description.replaceAll("<[^>]*>", "");
            etDescription.setText(description);
        }
        
        switchStatus.setChecked(job.isActive());

        // Set dates
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
            inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            
            if (job.getStartDate() != null && !job.getStartDate().isEmpty()) {
                Date startDate = inputFormat.parse(job.getStartDate());
                etStartDate.setText(outputFormat.format(startDate));
            }
            
            if (job.getEndDate() != null && !job.getEndDate().isEmpty()) {
                Date endDate = inputFormat.parse(job.getEndDate());
                etEndDate.setText(outputFormat.format(endDate));
            }
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing dates: " + e.getMessage());
        }

        // Set location
        String locationDisplay = "";
        for (Map.Entry<String, String> entry : locationMap.entrySet()) {
            if (entry.getValue().equals(job.getLocation())) {
                locationDisplay = entry.getKey();
                break;
            }
        }
        if (!locationDisplay.isEmpty()) {
            ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinnerLocation.getAdapter();
            int position = adapter.getPosition(locationDisplay);
            if (position != -1) {
                spinnerLocation.setSelection(position);
            }
        }

        // Set level
        if (job.getLevel() != null) {
            String levelDisplay = job.getLevel().name();
            ArrayAdapter<String> levelAdapter = (ArrayAdapter<String>) spinnerLevel.getAdapter();
            int levelPosition = levelAdapter.getPosition(levelDisplay);
            if (levelPosition != -1) {
                spinnerLevel.setSelection(levelPosition);
            }
        }

        // Set company
        if (job.getCompany() != null) {
            selectedCompany = job.getCompany();
            loadCompanies(); // Load lại danh sách công ty và set selection
        }

        // Set skills
        if (job.getSkills() != null && !job.getSkills().isEmpty()) {
            chipGroupSkills.removeAllViews();
            selectedSkillNames.clear();
            for (Skill jobSkill : job.getSkills()) {
                selectedSkillNames.add(jobSkill.getName());
                addChipToChipGroup(jobSkill.getName());
                
                // Update selectedSkillsFlags
                for (int i = 0; i < allSkillsList.size(); i++) {
                    if (allSkillsList.get(i).getId() == jobSkill.getId()) {
                        selectedSkillsFlags[i] = true;
                        break;
                    }
                }
            }
        }

        loadingDialog.dismiss();
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
    private void setupLevelSpinner() {
        List<String> levelsDisplay = new ArrayList<>();
        levelsDisplay.add("Hãy chọn Trình độ");
        for (LevelEnum level : LevelEnum.values()) {
            levelsDisplay.add(level.name());
        }
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
        loadingDialog.show("Đang cập nhật...");
        // Lấy dữ liệu từ các trường nhập liệu
        String name = etJobName.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String salaryStr = etSalary.getText().toString().trim().replace(",", ""); // Xóa dấu phẩy
        String quantityStr = etQuantity.getText().toString().trim();
        String startDate = etStartDate.getText().toString().trim();
        String endDate = etEndDate.getText().toString().trim();
        String locationDisplay = spinnerLocation.getSelectedItem().toString();
        String levelDisplay = spinnerLevel.getSelectedItem().toString();
        int companyPosition = spinnerCompany.getSelectedItemPosition();

        // Validate input
        if (name.isEmpty() || description.isEmpty() || salaryStr.isEmpty() || quantityStr.isEmpty()
                || locationDisplay.equals("Hãy chọn Địa điểm")
                || levelDisplay.equals("Hãy chọn Trình độ") || companyPosition == 0 || startDate.isEmpty() || endDate.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin bắt buộc.", Toast.LENGTH_LONG).show();
            loadingDialog.dismiss();
            return;
        }

        // Parse số
        double salary;
        int quantity;
        try {
            salary = Double.parseDouble(salaryStr);
            quantity = Integer.parseInt(quantityStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Mức lương và Số lượng phải là số hợp lệ.", Toast.LENGTH_LONG).show();
            loadingDialog.dismiss();
            return;
        }

        // Lấy trình độ
        String levelApiValue = levelDisplay.toUpperCase();

        // Lấy công ty
        if (spinnerCompany.getSelectedItemPosition() > 0 && allCompaniesList != null && !allCompaniesList.isEmpty()) {
            selectedCompany = allCompaniesList.get(spinnerCompany.getSelectedItemPosition() - 1); // Trừ 1 vì có hint
        }

        // Lấy skills
        List<Skill> selectedSkills = new ArrayList<>();
        for (int i = 0; i < allSkillsList.size(); i++) {
            if (selectedSkillsFlags[i]) {
                selectedSkills.add(allSkillsList.get(i));
            }
        }

        // Lấy location từ map
        String locationApiValue = locationMap.get(locationDisplay);

        // Parse dates
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        outputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        String formattedStartDate;
        String formattedEndDate;
        try {
            Date startDateObj = inputFormat.parse(startDate);
            Date endDateObj = inputFormat.parse(endDate);
            formattedStartDate = outputFormat.format(startDateObj);
            formattedEndDate = outputFormat.format(endDateObj);
        } catch (ParseException e) {
            Toast.makeText(this, "Lỗi định dạng ngày tháng.", Toast.LENGTH_LONG).show();
            loadingDialog.dismiss();
            return;
        }

        // Tạo job object
        Job job = new Job();
        job.setId(jobId);
        job.setName(name);
        job.setDescription(description);
        job.setSalary(salary);
        job.setQuantity(quantity);
        job.setLocation(locationApiValue);
        job.setLevel(LevelEnum.valueOf(levelApiValue));
        job.setCompany(selectedCompany);
        job.setSkills(selectedSkills);
        job.setStartDate(formattedStartDate);
        job.setEndDate(formattedEndDate);
        job.setActive(switchStatus.isChecked());

        // Gọi API update
        String token = sessionManager.getAuthToken();
        jobViewModel.updateJob(job, token);
    }
} 