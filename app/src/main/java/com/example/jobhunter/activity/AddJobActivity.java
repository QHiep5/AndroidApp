package com.example.jobhunter.activity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import com.example.jobhunter.util.constant.LevelEnum;

public class AddJobActivity extends AppCompatActivity {

    private ImageView btnBack;
    private EditText etJobName, etLocation, etSalary, etQuantity, etDescription;
    private TextView tvSelectSkills;
    private ChipGroup chipGroupSkills;
    private Spinner spinnerLevel, spinnerCompany;
    private EditText etStartDate, etEndDate;
    private Switch switchStatus;
    private Button btnAddNewJob, btnCancel;

    private JobViewModel jobViewModel;
    private SkillViewModel skillViewModel;
    private CompanyViewModel companyViewModel;
    private SessionManager sessionManager;

    private List<Skill> allSkillsList = new ArrayList<>();
    private boolean[] selectedSkillsFlags;
    private ArrayList<String> selectedSkillNames = new ArrayList<>(); // To store selected skill names

    private List<Company> allCompaniesList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_job);

        sessionManager = new SessionManager(this);

        // Ánh xạ Views
        btnBack = findViewById(R.id.btn_back);
        etJobName = findViewById(R.id.et_job_name);
        tvSelectSkills = findViewById(R.id.tv_select_skills);
        chipGroupSkills = findViewById(R.id.chip_group_skills);
        etLocation = findViewById(R.id.et_location);
        etSalary = findViewById(R.id.et_salary);
        etQuantity = findViewById(R.id.et_quantity);
        spinnerLevel = findViewById(R.id.spinner_level);
        spinnerCompany = findViewById(R.id.spinner_company);
        etStartDate = findViewById(R.id.et_start_date);
        etEndDate = findViewById(R.id.et_end_date);
        etDescription = findViewById(R.id.et_description);
        switchStatus = findViewById(R.id.switch_status);
        btnAddNewJob = findViewById(R.id.btn_add_new_job);
        btnCancel = findViewById(R.id.btn_cancel);

        // Setup ViewModel
        jobViewModel = new ViewModelProvider(this).get(JobViewModel.class);
        skillViewModel = new ViewModelProvider(this).get(SkillViewModel.class);
        companyViewModel = new ViewModelProvider(this).get(CompanyViewModel.class);

        // Load data for Spinners and Chips
        loadSkills();
        loadCompanies();
        setupLevelSpinner();

        // Xử lý sự kiện
        btnBack.setOnClickListener(v -> onBackPressed());

        etStartDate.setOnClickListener(v -> showDatePickerDialog(etStartDate));
        etEndDate.setOnClickListener(v -> showDatePickerDialog(etEndDate));

        tvSelectSkills.setOnClickListener(v -> showSkillSelectionDialog());

        btnAddNewJob.setOnClickListener(v -> addNewJob());
        btnCancel.setOnClickListener(v -> finish()); // Đóng activity hiện tại
    }

    private void loadSkills() {
        skillViewModel.getSkillsLiveData().observe(this, skills -> {
            if (skills != null) {
                allSkillsList.clear();
                allSkillsList.addAll(skills);
                selectedSkillsFlags = new boolean[allSkillsList.size()]; // Initialize flags
            }
        });
        skillViewModel.fetchSkills(); // Kích hoạt fetch skills
    }

    private void loadCompanies() {
        companyViewModel.getCompaniesLiveData().observe(this, companies -> {
            if (companies != null) {
                allCompaniesList.clear();
                allCompaniesList.addAll(companies);
                List<String> companyNames = new ArrayList<>();
                for (Company company : companies) {
                    companyNames.add(company.getName());
                }
                ArrayAdapter<String> companyAdapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_item, companyNames);
                companyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCompany.setAdapter(companyAdapter);
            }
        });
        String token = sessionManager.getAuthToken();
        companyViewModel.fetchCompanies(token); // Kích hoạt fetch companies
    }

    private void setupLevelSpinner() {
        String[] levels = {"INTERN", "FRESHER", "JUNIOR", "MIDDLE", "SENIOR"}; // Các cấp độ từ Enum LevelEnum của bạn
        ArrayAdapter<String> levelAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, levels);
        levelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLevel.setAdapter(levelAdapter);
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
                    selectedSkillNames.add(selectedSkill.getName()); // Add name for display
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
            // Cập nhật lại selectedSkillsFlags nếu cần, dựa trên tên
            for (int i = 0; i < allSkillsList.size(); i++) {
                if (allSkillsList.get(i).getName().equals(skillName)) {
                    selectedSkillsFlags[i] = false;
                    break;
                }
            }
        });
        chipGroupSkills.addView(chip);
    }

    private void addNewJob() {
        String name = etJobName.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String salaryStr = etSalary.getText().toString().trim();
        String quantityStr = etQuantity.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String startDate = etStartDate.getText().toString().trim();
        String endDate = etEndDate.getText().toString().trim();
        boolean isActive = switchStatus.isChecked();

        // Validate inputs (simplistic validation)
        if (name.isEmpty() || location.isEmpty() || salaryStr.isEmpty() || quantityStr.isEmpty() ||
            description.isEmpty() || startDate.isEmpty() || endDate.isEmpty() || selectedSkillNames.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin và chọn kỹ năng.", Toast.LENGTH_LONG).show();
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

        String level = spinnerLevel.getSelectedItem() != null ? spinnerLevel.getSelectedItem().toString() : "";
        Company selectedCompany = null;
        if (spinnerCompany.getSelectedItemPosition() != Spinner.INVALID_POSITION &&
            allCompaniesList != null && !allCompaniesList.isEmpty()) {
            selectedCompany = allCompaniesList.get(spinnerCompany.getSelectedItemPosition());
        }

        if (selectedCompany == null) {
            Toast.makeText(this, "Vui lòng chọn công ty.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo đối tượng Job
        Job newJob = new Job();
        newJob.setName(name);
        newJob.setLocation(location);
        newJob.setSalary(salary);
        newJob.setQuantity(quantity);
        newJob.setLevel(LevelEnum.valueOf(level)); // Chuyển đổi String sang Enum
        newJob.setDescription(description);
        newJob.setStartDate(startDate);
        newJob.setEndDate(endDate);
        newJob.setActive(isActive);
        newJob.setCompany(selectedCompany);

        // Lấy danh sách Skill objects từ tên đã chọn
        List<Skill> jobSkills = new ArrayList<>();
        for (String skillName : selectedSkillNames) {
            for (Skill s : allSkillsList) {
                if (s.getName().equals(skillName)) {
                    jobSkills.add(s);
                    break;
                }
            }
        }
        newJob.setSkills(jobSkills);

        // TODO: Gọi API để thêm job mới
        // jobViewModel.createNewJob(newJob, sessionManager.getAuthToken());

        // Theo dõi kết quả từ ViewModel
        jobViewModel.getCreateJobResultLiveData().observe(this, success -> {
            if (success != null && success) {
                Toast.makeText(AddJobActivity.this, "Thêm công việc thành công!", Toast.LENGTH_SHORT).show();
                finish(); // Đóng Activity sau khi thêm thành công
            } else {
                Toast.makeText(AddJobActivity.this, "Thêm công việc thất bại. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
            }
        });

        // Uncomment dòng dưới để gửi API request thực tế
        // jobViewModel.createJob(newJob, sessionManager.getAuthToken());
        Toast.makeText(this, "Đã cố gắng thêm công việc: " + newJob.getName(), Toast.LENGTH_LONG).show();
    }
} 