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
import com.example.jobhunter.R;
import com.example.jobhunter.ViewModel.UserViewModelDetails;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.Toast;

public class ProfileDetailActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_details);

        int userId = getIntent().getIntExtra("user_id", 0);
        Log.d("userIDTrongProfileDetails",String.valueOf(userId));
        TextView tvName = findViewById(R.id.tv_name);
        TextView tvAge = findViewById(R.id.tv_age);
        TextView tvSalary = findViewById(R.id.tv_salary);
        TextView tvCompany = findViewById(R.id.tv_company);
        LinearLayout layoutSkills = findViewById(R.id.layout_skills);
        // Các trường cho phép chỉnh sửa
        android.widget.EditText etEmail = findViewById(R.id.et_email);
        android.widget.EditText etAddress = findViewById(R.id.et_address);
        Button btnEdit = findViewById(R.id.btn_edit);
        Button btnCancel = findViewById(R.id.btn_cancel);
        Spinner spinnerGender = findViewById(R.id.spinner_gender);
        Spinner spinnerLevel = findViewById(R.id.spinner_level);

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
                tvSalary.setText(user.getSalary() != 0 ? String.valueOf(user.getSalary()) : "");
                // Set gender spinner
                if (user.getGender() != null) {
                    int genderPos = genderAdapter.getPosition(user.getGender().name());
                    spinnerGender.setSelection(genderPos);
                }
                etAddress.setText(user.getAddress() != null ? user.getAddress() : "");
//                tvAge.setText(user.getAge() > 0 ? String.valueOf(user.getAge()) : "");
                tvSalary.setText(user.getSalary() != 0 ? String.valueOf(user.getSalary()) : "");
                // Set level spinner
                if (user.getLevel() != null) {
                    int levelPos = levelAdapter.getPosition(user.getLevel().name());
                    spinnerLevel.setSelection(levelPos);
                }
                if (user.getCompany() != null && user.getCompany().getName() != null && !user.getCompany().getName().isEmpty()) {
                    tvCompany.setText(user.getCompany().getName());
                    tvCompany.setVisibility(TextView.VISIBLE);
                } else {
                    tvCompany.setVisibility(TextView.GONE);
                }
                layoutSkills.removeAllViews();
                if (user.getSkills() != null && !user.getSkills().isEmpty()) {
                    for (com.example.jobhunter.model.Skill skill : user.getSkills()) {
                        TextView skillView = new TextView(this);
                        skillView.setText("- " + skill.getName());
                        skillView.setTextSize(15f);
                        layoutSkills.addView(skillView);
                    }
                }
                // Lưu giá trị gốc để huỷ
                originalValues[0] = etEmail.getText().toString();
                originalValues[1] = etAddress.getText().toString();
                originalValues[2] = String.valueOf(spinnerLevel.getSelectedItemPosition());
                originalValues[3] = String.valueOf(spinnerGender.getSelectedItemPosition());
            }
        });

        btnEdit.setOnClickListener(v -> {
            if (!isEditing[0]) {
                // Bắt đầu chỉnh sửa
                isEditing[0] = true;
                btnEdit.setText("Hoàn tất");
                etEmail.setEnabled(true);
                etAddress.setEnabled(true);
                spinnerLevel.setEnabled(true);
                spinnerGender.setEnabled(true);
                btnCancel.setEnabled(true);
                btnCancel.setAlpha(1f);
            } else {
                // Kết thúc chỉnh sửa, lấy dữ liệu và gọi API update
                isEditing[0] = false;
                btnEdit.setText("Chỉnh sửa");
                etEmail.setEnabled(false);
                etAddress.setEnabled(false);
                spinnerLevel.setEnabled(false);
                spinnerGender.setEnabled(false);
                btnCancel.setEnabled(false);
                btnCancel.setAlpha(0.5f);
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
            }
        });
    }
} 