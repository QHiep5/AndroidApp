package com.example.jobhunter.activity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import com.example.jobhunter.util.constant.LevelEnum;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AddJobActivity extends AppCompatActivity {

    private static final String TAG = "AddJobActivity";
    private ImageView btnBack;
    private EditText etJobName, etSalary, etQuantity, etDescription;
    private TextView tvSelectSkills;
    private ChipGroup chipGroupSkills;
    private Spinner spinnerLevel, spinnerCompany, spinnerLocation;
    private EditText etStartDate, etEndDate;
    private Switch switchStatus;
    private Button btnAddNewJob, btnCancel;

    private JobViewModel jobViewModel;
    private SkillViewModel skillViewModel;
    private CompanyViewModel companyViewModel;
    private SessionManager sessionManager;

    private List<Skill> allSkillsList = new ArrayList<>();
    private boolean[] selectedSkillsFlags;
    private ArrayList<String> selectedSkillNames = new ArrayList<>();

    private List<Company> allCompaniesList = new ArrayList<>();
    private Map<String, String> locationMap = new LinkedHashMap<>();

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
        spinnerLocation = findViewById(R.id.spinner_location); // Ánh xạ Spinner Địa điểm
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
        setupLocationSpinner(); // Setup spinner địa điểm

        // Xử lý sự kiện
        btnBack.setOnClickListener(v -> onBackPressed());

        etStartDate.setOnClickListener(v -> showDatePickerDialog(etStartDate));
        etEndDate.setOnClickListener(v -> showDatePickerDialog(etEndDate));

        tvSelectSkills.setOnClickListener(v -> showSkillSelectionDialog());

        btnAddNewJob.setOnClickListener(v -> addNewJob());
        btnCancel.setOnClickListener(v -> finish()); // Đóng activity hiện tại

        // Theo dõi kết quả từ ViewModel
        jobViewModel.getCreateJobResultLiveData().observe(this, success -> {
            Log.d("AddJobActivity", "CreateJobResultLiveData triggered. Success = " + success);

            if (success != null && success) {
                Toast.makeText(AddJobActivity.this, "Thêm công việc thành công!", Toast.LENGTH_SHORT).show();
                Log.d("AddJobActivity", "Công việc được thêm thành công. Đóng Activity.");
                finish(); // Đóng Activity sau khi thêm thành công
            } else {
                Toast.makeText(AddJobActivity.this, "Thêm công việc thất bại. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
                Log.w("AddJobActivity", "Thêm công việc thất bại.");
            }
        });

        jobViewModel.getErrorLiveData().observe(this, error -> {
            Log.d("AddJobActivity", "ErrorLiveData triggered. Error = " + error);
            if (error != null && !error.isEmpty()) {
                Toast.makeText(AddJobActivity.this, "Lỗi: " + error, Toast.LENGTH_LONG).show();
                Log.e("AddJobActivity", "Lỗi khi tạo công việc: " + error);
            }
        });

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
                companyNames.add("Hãy chọn Công Ty"); // Hint
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
        List<String> levelsDisplay = new ArrayList<>(Arrays.asList("Hãy chọn Trình độ", "Intern", "Fresher", "Junior", "Middle", "Senior"));
        ArrayAdapter<String> levelAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, levelsDisplay);
        levelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLevel.setAdapter(levelAdapter);
    }

    private void setupLocationSpinner() {
        locationMap.put("Hãy chọn Địa điểm", ""); // Hint
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
                    // Hiển thị ngày tháng năm cho người dùng
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

    private void addNewJob() {
        String name = etJobName.getText().toString().trim();
        String salaryStr = etSalary.getText().toString().trim();
        String quantityStr = etQuantity.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        boolean isActive = switchStatus.isChecked();

        // Lấy giá trị từ Spinner Địa điểm
        String selectedLocationDisplay = spinnerLocation.getSelectedItem() != null ? spinnerLocation.getSelectedItem().toString() : "";
        String location = locationMap.get(selectedLocationDisplay); // Lấy giá trị API

        // Lấy giá trị từ Spinner Trình độ
        String levelDisplay = spinnerLevel.getSelectedItem() != null ? spinnerLevel.getSelectedItem().toString() : "";
        String levelApiValue = "";
        if (!levelDisplay.equals("Hãy chọn Trình độ")) {
            levelApiValue = levelDisplay.toUpperCase(Locale.ROOT); // Chuyển đổi sang uppercase cho API
        }

        // Lấy giá trị từ Spinner Công ty
        Company selectedCompany = null;
        if (spinnerCompany.getSelectedItemPosition() > 0 && allCompaniesList != null && !allCompaniesList.isEmpty()) {
            selectedCompany = allCompaniesList.get(spinnerCompany.getSelectedItemPosition() - 1); // Trừ 1 vì có hint
        }

        // Validate inputs
        if (name.isEmpty() || location == null || location.isEmpty() || salaryStr.isEmpty() || quantityStr.isEmpty() ||
                description.isEmpty() || etStartDate.getText().toString().isEmpty() || etEndDate.getText().toString().isEmpty() ||
                selectedSkillNames.isEmpty() || levelApiValue.isEmpty() || selectedCompany == null) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin và chọn các mục bắt buộc.", Toast.LENGTH_LONG).show();
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

        // Tạo đối tượng Job
        Job newJob = new Job();
        newJob.setName(name);
        newJob.setLocation(location);
        newJob.setSalary(salary);
        newJob.setQuantity(quantity);
        newJob.setLevel(LevelEnum.valueOf(levelApiValue));
        newJob.setDescription(description);
        newJob.setActive(isActive);
        newJob.setCompany(selectedCompany);

        // Chuyển đổi ngày tháng sang định dạng ISO 8601 (yyyy-MM-dd'T'HH:mm:ss.SSS'Z')
        SimpleDateFormat sdfDateOnly = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat isoDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        isoDateFormat.setTimeZone(TimeZone.getTimeZone("UTC")); // Đảm bảo múi giờ UTC

        try {
            Date startDateObj = sdfDateOnly.parse(etStartDate.getText().toString());
            Date endDateObj = sdfDateOnly.parse(etEndDate.getText().toString());

            // Để có phần giờ, phút, giây, mili giây, ta có thể dùng thời gian hiện tại
            // hoặc đặt một giá trị cố định (ví dụ: 00:00:00.000 UTC).
            // Dựa trên mẫu JSON bạn cung cấp, có vẻ backend mong đợi một thời gian cụ thể.
            // Tôi sẽ sử dụng thời gian hiện tại (tại thời điểm submit) nhưng vẫn đảm bảo UTC.

            Calendar currentUtcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

            Calendar startCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            startCal.setTime(startDateObj);
            startCal.set(Calendar.HOUR_OF_DAY, currentUtcCalendar.get(Calendar.HOUR_OF_DAY));
            startCal.set(Calendar.MINUTE, currentUtcCalendar.get(Calendar.MINUTE));
            startCal.set(Calendar.SECOND, currentUtcCalendar.get(Calendar.SECOND));
            startCal.set(Calendar.MILLISECOND, currentUtcCalendar.get(Calendar.MILLISECOND));

            Calendar endCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            endCal.setTime(endDateObj);
            endCal.set(Calendar.HOUR_OF_DAY, currentUtcCalendar.get(Calendar.HOUR_OF_DAY));
            endCal.set(Calendar.MINUTE, currentUtcCalendar.get(Calendar.MINUTE));
            endCal.set(Calendar.SECOND, currentUtcCalendar.get(Calendar.SECOND));
            endCal.set(Calendar.MILLISECOND, currentUtcCalendar.get(Calendar.MILLISECOND));

            newJob.setStartDate(isoDateFormat.format(startCal.getTime()));
            newJob.setEndDate(isoDateFormat.format(endCal.getTime()));

        } catch (ParseException e) {
            Log.e(TAG, "Lỗi định dạng ngày tháng khi parse: ", e);
            Toast.makeText(this, "Lỗi định dạng ngày tháng. Vui lòng chọn lại.", Toast.LENGTH_LONG).show();
            return;
        }


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

        // Log JSON object for debugging
        JSONObject jobJson = new JSONObject();
        try {
            jobJson.put("name", newJob.getName());
            jobJson.put("location", newJob.getLocation());
            jobJson.put("salary", newJob.getSalary());
            jobJson.put("quantity", newJob.getQuantity());
            jobJson.put("level", newJob.getLevel().name());
            jobJson.put("description", newJob.getDescription());
            jobJson.put("startDate", newJob.getStartDate());
            jobJson.put("endDate", newJob.getEndDate());
            jobJson.put("active", newJob.isActive());
            jobJson.put("companyId", newJob.getCompany().getId());

            JSONArray skillIdsArray = new JSONArray();
            if (newJob.getSkills() != null) {
                for (Skill skill : newJob.getSkills()) {
                    skillIdsArray.put(skill.getId());
                }
            }
            jobJson.put("skills", skillIdsArray);

            Log.d(TAG, "Sending JSON for new job: " + jobJson.toString(2)); // Log với định dạng dễ đọc

        } catch (JSONException e) {
            Log.e(TAG, "Error creating JSON for log: ", e);
        }

        // Gọi API để thêm job mới
        jobViewModel.createJob(newJob, sessionManager.getAuthToken());
    }
}