package com.example.jobhunter.activity;

import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Button;
import android.text.TextUtils;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.Observer;
import com.example.jobhunter.R;
import com.example.jobhunter.ViewModel.UserViewModelDetails;
import com.example.jobhunter.ViewModel.SkillViewModel;
import com.example.jobhunter.model.Skill;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import android.widget.ImageView;
import android.app.AlertDialog;
import java.util.List;
<<<<<<< HEAD
import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.JustifyContent;
import com.google.android.flexbox.AlignItems;
=======
>>>>>>> 63ae649 (View + Feat : Profile Detail)

public class ProfileDetailActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_details);

        int userId = getIntent().getIntExtra("user_id", 0);
        Log.d("userIDTrongProfileDetails",String.valueOf(userId));
        TextView tvName = findViewById(R.id.tv_name);
        TextView tvAge = findViewById(R.id.tv_age);
//        TextView tvSalary = findViewById(R.id.tv_salary);
        TextView tvCompany = findViewById(R.id.tv_company);
        TextView tvCompanyLabel = findViewById(R.id.tv_company_label);
        FlexboxLayout layoutSkillsRow = findViewById(R.id.layout_skills_row);
        // Các trường cho phép chỉnh sửa
        android.widget.EditText etEmail = findViewById(R.id.et_email);
        android.widget.EditText etAddress = findViewById(R.id.et_address);
        Button btnEdit = findViewById(R.id.btn_edit);
        Button btnCancel = findViewById(R.id.btn_cancel);
        Spinner spinnerGender = findViewById(R.id.spinner_gender);
        Spinner spinnerLevel = findViewById(R.id.spinner_level);
        ImageView btnBack = findViewById(R.id.btn_back);
        Button btnEditSkills = findViewById(R.id.btn_edit_skills);

        // Chuẩn bị dữ liệu enum cho spinner
        String[] genderArray = new String[com.example.jobhunter.util.constant.GenderEnum.values().length];
        for (int i = 0; i < com.example.jobhunter.util.constant.GenderEnum.values().length; i++) {
            genderArray[i] = com.example.jobhunter.util.constant.GenderEnum.values()[i].name();
        }
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, genderArray);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(genderAdapter);
        spinnerGender.setEnabled(false);

        String[] levelArray = new String[com.example.jobhunter.util.constant.LevelEnum.values().length];
        for (int i = 0; i < com.example.jobhunter.util.constant.LevelEnum.values().length; i++) {
            levelArray[i] = com.example.jobhunter.util.constant.LevelEnum.values()[i].name();
        }

        ArrayAdapter<String> levelAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, levelArray);
        levelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLevel.setAdapter(levelAdapter);
        spinnerLevel.setEnabled(false);

        // Ban đầu không cho chỉnh sửa
        etEmail.setEnabled(false);
        etAddress.setEnabled(false);
        btnCancel.setEnabled(false);
        btnCancel.setAlpha(0.5f);
        final boolean[] isEditing = {false};
        final String[] originalValues = new String[4];

        UserViewModelDetails userViewModelDetails = new ViewModelProvider(this).get(UserViewModelDetails.class);
        userViewModelDetails.getUserDetails(userId).observe(this, user -> {
            Log.d("userDetails",user.getName());
            if (user != null) {
                tvName.setText(user.getName() != null ? user.getName() : "");
                etEmail.setText(user.getEmail() != null ? user.getEmail() : "");
                tvAge.setText(user.getAge()!= 0 ? String.valueOf(user.getAge()) : "20");
//                tvSalary.setText(user.getSalary() != 0 ? String.valueOf(user.getSalary()) : "");
                // Set gender spinner
                if (user.getGender() != null) {
                    int genderPos = genderAdapter.getPosition(user.getGender().name());
                    spinnerGender.setSelection(genderPos);
                }
                etAddress.setText(user.getAddress() != null ? user.getAddress() : "");
//                tvAge.setText(user.getAge() > 0 ? String.valueOf(user.getAge()) : "");
//                tvSalary.setText(user.getSalary() != 0 ? String.valueOf(user.getSalary()) : "");
                // Set level spinner
                if (user.getLevel() != null) {
                    int levelPos = levelAdapter.getPosition(user.getLevel().name());
                    spinnerLevel.setSelection(levelPos);
                }
                if (user.getCompany() != null && user.getCompany().getName() != null && !user.getCompany().getName().isEmpty()) {
                    tvCompany.setText(user.getCompany().getName());
                    tvCompany.setVisibility(TextView.VISIBLE);
                    tvCompanyLabel.setVisibility(TextView.VISIBLE);
                } else {
                    tvCompany.setVisibility(TextView.GONE);
                    tvCompanyLabel.setVisibility(TextView.GONE);
                }
                layoutSkillsRow.removeAllViews();
                if (user.getSkills() != null && !user.getSkills().isEmpty()) {
                    for (com.example.jobhunter.model.Skill skill : user.getSkills()) {
                        TextView skillView = new TextView(this);
                        skillView.setText(skill.getName());
                        skillView.setTextSize(15f);
                        skillView.setTextColor(getResources().getColor(R.color.colorPrimary));
                        skillView.setBackgroundResource(R.drawable.bg_skill_chip);
                        skillView.setPadding(32, 8, 32, 8);
                        
                        // Create FlexboxLayout parameters
                        FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(
                            FlexboxLayout.LayoutParams.WRAP_CONTENT,
                            FlexboxLayout.LayoutParams.WRAP_CONTENT
                        );
                        params.setMargins(0, 0, 16, 8); // Add bottom margin for spacing between rows
                        skillView.setLayoutParams(params);
                        
                        layoutSkillsRow.addView(skillView);
                    }
                }
                // Lưu giá trị gốc để huỷ
                originalValues[0] = etEmail.getText().toString();
                originalValues[1] = etAddress.getText().toString();
                originalValues[2] = String.valueOf(spinnerLevel.getSelectedItemPosition());
                originalValues[3] = String.valueOf(spinnerGender.getSelectedItemPosition());
            }
        });

        // Initially disable edit skills button
        btnEditSkills.setEnabled(false);
        btnEditSkills.setAlpha(0.5f);

        // Store selected skills temporarily
        List<Skill> selectedSkills = new ArrayList<>();

        btnEdit.setOnClickListener(v -> {
            if (!isEditing[0]) {
                // Bắt đầu chỉnh sửa
                isEditing[0] = true;
                btnEdit.setText("Hoàn tất");
                etAddress.setEnabled(true);
                spinnerLevel.setEnabled(true);
                spinnerGender.setEnabled(true);
                btnCancel.setEnabled(true);
                btnCancel.setAlpha(1f);
                // Enable edit skills button
                btnEditSkills.setEnabled(true);
                btnEditSkills.setAlpha(1f);
            } else {
                // Kết thúc chỉnh sửa, lấy dữ liệu và gọi API update
                isEditing[0] = false;
                btnEdit.setText("Chỉnh sửa");
                etAddress.setEnabled(false);
                spinnerLevel.setEnabled(false);
                spinnerGender.setEnabled(false);
                btnCancel.setEnabled(false);
                btnCancel.setAlpha(0.5f);
                // Disable edit skills button
                btnEditSkills.setEnabled(false);
                btnEditSkills.setAlpha(0.5f);

                // Lấy dữ liệu và gọi update
                String email = etEmail.getText().toString();
                String address = etAddress.getText().toString();
                String levelStr = spinnerLevel.getSelectedItem().toString();
                String genderStr = spinnerGender.getSelectedItem().toString();
                String name = tvName.getText().toString();
                long id = userId;
                // Lấy tuổi
                long age = 0;
                try {
                    age = Long.parseLong(tvAge.getText().toString());
                } catch (Exception ignored) {}
                com.example.jobhunter.util.constant.LevelEnum level = com.example.jobhunter.util.constant.LevelEnum.valueOf(levelStr);
                com.example.jobhunter.util.constant.GenderEnum gender = com.example.jobhunter.util.constant.GenderEnum.valueOf(genderStr);
                com.example.jobhunter.model.User userUpdate = new com.example.jobhunter.model.User();

                userUpdate.setId(id);
                userUpdate.setName(name);
                userUpdate.setEmail(email);
                userUpdate.setAge(age);
                userUpdate.setGender(gender);
                userUpdate.setAddress(address);
                userUpdate.setLevel(level);
                userUpdate.setSkills(selectedSkills); // Add selected skills to update
                userViewModelDetails.updateUser(userUpdate);
            }
        });
        userViewModelDetails.getUpdateResultLiveData().observe(this, success -> {
            if (success == null) return;
            if (success) {
                Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Cập nhật thất bại!", Toast.LENGTH_SHORT).show();
            }
        });
        btnCancel.setOnClickListener(v -> {
            if (isEditing[0]) {
                // Huỷ chỉnh sửa, trả lại giá trị gốc
                etEmail.setText(originalValues[0]);
                etAddress.setText(originalValues[1]);
                spinnerLevel.setSelection(Integer.parseInt(originalValues[2]));
                spinnerGender.setSelection(Integer.parseInt(originalValues[3]));
                etEmail.setEnabled(false);
                etAddress.setEnabled(false);
                spinnerLevel.setEnabled(false);
                spinnerGender.setEnabled(false);
                btnEdit.setText("Chỉnh sửa");
                isEditing[0] = false;
                btnCancel.setEnabled(false);
                btnCancel.setAlpha(0.5f);
                // Disable edit skills button
                btnEditSkills.setEnabled(false);
                btnEditSkills.setAlpha(0.5f);
                // Reset selected skills to original
                selectedSkills.clear();
                if (userViewModelDetails.getUserDetails(userId).getValue() != null) {
                    selectedSkills.addAll(userViewModelDetails.getUserDetails(userId).getValue().getSkills());
                }
                // Refresh skills display
                updateSkillsDisplay(layoutSkillsRow, selectedSkills);
            }
        });
        btnBack.setOnClickListener(v -> finish());

        SkillViewModel skillViewModel = new ViewModelProvider(this).get(SkillViewModel.class);
        btnEditSkills.setOnClickListener(v -> {
            skillViewModel.fetchSkills();
            skillViewModel.getSkillsLiveData().observe(this, new Observer<List<Skill>>() {
                @Override
                public void onChanged(List<Skill> skills) {
                    if (skills == null || skills.isEmpty()) {
                        Toast.makeText(ProfileDetailActivity.this, "Không có dữ liệu kỹ năng!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String[] skillNames = new String[skills.size()];
                    boolean[] checkedItems = new boolean[skills.size()];
                    
                    // Set initial checked state based on selectedSkills
                    Set<Long> selectedSkillIds = new HashSet<>();
                    for (Skill skill : selectedSkills) {
                        selectedSkillIds.add(skill.getId());
                    }
                    
                    for (int i = 0; i < skills.size(); i++) {
                        skillNames[i] = skills.get(i).getName();
                        checkedItems[i] = selectedSkillIds.contains(skills.get(i).getId());
                    }

                    AlertDialog.Builder builder = new AlertDialog.Builder(ProfileDetailActivity.this);
                    builder.setTitle("Chọn kỹ năng")
                        .setMultiChoiceItems(skillNames, checkedItems, (dialog, which, isChecked) -> {
                            // Handle selection change if needed
                        })
                        .setPositiveButton("Lưu", (dialog, which) -> {
                            // Update selected skills
                            selectedSkills.clear();
                            for (int i = 0; i < skills.size(); i++) {
                                if (checkedItems[i]) {
                                    selectedSkills.add(skills.get(i));
                                }
                            }
                            
                            // Update skills display immediately
                            updateSkillsDisplay(layoutSkillsRow, selectedSkills);
                        })
                        .setNegativeButton("Huỷ", null)
                        .show();
                    
                    // Remove observer after showing dialog
                    skillViewModel.getSkillsLiveData().removeObserver(this);
                }
            });
        });

        // Initialize selected skills with user's current skills
        userViewModelDetails.getUserDetails(userId).observe(this, user -> {
            if (user != null) {
                selectedSkills.clear();
                if (user.getSkills() != null) {
                    selectedSkills.addAll(user.getSkills());
                }
                // Update initial skills display
                updateSkillsDisplay(layoutSkillsRow, selectedSkills);
            }
        });
        btnBack.setOnClickListener(v -> finish());

        SkillViewModel skillViewModel = new ViewModelProvider(this).get(SkillViewModel.class);
        btnEditSkills.setOnClickListener(v -> {
            skillViewModel.fetchSkills();
            skillViewModel.getSkillsLiveData().observe(this, new Observer<List<Skill>>() {
                @Override
                public void onChanged(List<Skill> skills) {
                    if (skills == null || skills.isEmpty()) {
                        Toast.makeText(ProfileDetailActivity.this, "Không có dữ liệu kỹ năng!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String[] skillNames = new String[skills.size()];
                    boolean[] checkedItems = new boolean[skills.size()];
                    for (int i = 0; i < skills.size(); i++) {
                        skillNames[i] = skills.get(i).getName();
                        checkedItems[i] = false; // TODO: Đánh dấu true nếu user đã có kỹ năng này
                    }
                    new AlertDialog.Builder(ProfileDetailActivity.this)
                        .setTitle("Chọn kỹ năng")
                        .setMultiChoiceItems(skillNames, checkedItems, (dialog, which, isChecked) -> {
                            // Có thể xử lý chọn/bỏ chọn ở đây nếu muốn
                        })
                        .setPositiveButton("OK", (dialog, which) -> {
                            // TODO: Lưu lại danh sách kỹ năng đã chọn
                            StringBuilder sb = new StringBuilder("Kỹ năng đã chọn: ");
                            for (int i = 0; i < skillNames.length; i++) {
                                if (checkedItems[i]) sb.append(skillNames[i]).append(", ");
                            }
                            Toast.makeText(ProfileDetailActivity.this, sb.toString(), Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Huỷ", null)
                        .show();
                    // Chỉ observe 1 lần
                    skillViewModel.getSkillsLiveData().removeObserver(this);
                }
            });
        });
    }

    // Function to update skills display
    private void updateSkillsDisplay(FlexboxLayout layout, List<Skill> skills) {
        layout.removeAllViews();
        if (skills != null && !skills.isEmpty()) {
            for (Skill skill : skills) {
                TextView skillView = new TextView(this);
                skillView.setText(skill.getName());
                skillView.setTextSize(15f);
                skillView.setTextColor(getResources().getColor(R.color.colorPrimary));
                skillView.setBackgroundResource(R.drawable.bg_skill_chip);
                skillView.setPadding(32, 8, 32, 8);
                
                // Create FlexboxLayout parameters
                FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(
                    FlexboxLayout.LayoutParams.WRAP_CONTENT,
                    FlexboxLayout.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(0, 0, 16, 8); // Add bottom margin for spacing between rows
                skillView.setLayoutParams(params);
                
                layout.addView(skillView);
            }
        }
    }
} 