package com.example.jobhunter.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.jobhunter.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.example.jobhunter.api.ApiConfig;

import org.json.JSONException;
import org.json.JSONObject;

public class RegisterActivity extends AppCompatActivity {
    private TextInputEditText etName, etEmail, etPassword, etConfirmPassword;
    private Button btnRegister;
    private TextView tvLogin, tvGuest;
    private CheckBox cbAgree;
    private ProgressBar progressBar;
    private RequestQueue requestQueue;
    private static final String TAG = "RegisterActivity";
    private TextInputLayout tilName, tilEmail, tilPassword, tilConfirmPassword;
    private Handler errorHandler = new Handler();
    private Runnable clearNameError, clearEmailError, clearPasswordError, clearConfirmError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Khởi tạo RequestQueue
        requestQueue = Volley.newRequestQueue(this);

        // Ánh xạ view đúng với layout mới
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);
        tvGuest = findViewById(R.id.tvGuest);
        cbAgree = findViewById(R.id.cbAgree);
        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleLarge);
        addContentView(progressBar, new android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.WRAP_CONTENT,
                android.widget.FrameLayout.LayoutParams.WRAP_CONTENT,
                android.view.Gravity.CENTER));
        progressBar.setVisibility(View.GONE);

        tilName = findViewById(R.id.tilName);
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);

        // Realtime validation
        android.text.TextWatcher watcher = new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Hiển thị lỗi mật khẩu không khớp realtime
                String password = etPassword.getText().toString();
                String confirm = etConfirmPassword.getText().toString();
                if (!confirm.isEmpty() && !password.equals(confirm)) {
                    tilConfirmPassword.setError("Mật khẩu không khớp");
                    errorHandler.removeCallbacks(clearConfirmError);
                    errorHandler.postDelayed(clearConfirmError, 3000);
                } else {
                    tilConfirmPassword.setError(null);
                    errorHandler.removeCallbacks(clearConfirmError);
                }
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {
            }
        };
        etName.addTextChangedListener(watcher);
        etEmail.addTextChangedListener(watcher);
        etPassword.addTextChangedListener(watcher);
        etConfirmPassword.addTextChangedListener(watcher);
        // Không disable nút đăng ký khi chưa tick điều khoản
        btnRegister.setEnabled(true);

        // Xử lý sự kiện đăng ký
        btnRegister.setOnClickListener(v -> registerUser());

        // Xử lý sự kiện chuyển đến màn hình đăng nhập
        tvLogin.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivityWithAnim(intent);
            finish();
        });

        // Xử lý sự kiện trải nghiệm không cần đăng nhập
        tvGuest.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
            startActivityWithAnim(intent);
            finish();
        });

        clearNameError = () -> tilName.setError(null);
        clearEmailError = () -> tilEmail.setError(null);
        clearPasswordError = () -> tilPassword.setError(null);
        clearConfirmError = () -> tilConfirmPassword.setError(null);
    }

    private void registerUser() {
        if (!validateInput(true)) {
            return;
        }
        btnRegister.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        // Tạo JSON object chứa dữ liệu đăng ký
        JSONObject registerData = new JSONObject();
        try {
            registerData.put("name", etName.getText().toString().trim());
            registerData.put("email", etEmail.getText().toString().trim());
            registerData.put("password", etPassword.getText().toString().trim());

            // Thêm company object
            JSONObject company = new JSONObject();
            company.put("name", "");
            company.put("position", "");
            company.put("salary", 0);
            company.put("level", "");
            company.put("skills", "");
            registerData.put("company", company);

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi tạo dữ liệu đăng ký", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            btnRegister.setEnabled(true);
            return;
        }
        String url = ApiConfig.AUTH_REGISTER;
        Log.d("REGISTER_URL", "URL: " + url);
        Log.d("REGISTER_DATA", "Request data: " + registerData.toString());
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, registerData,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    btnRegister.setEnabled(true);
                    try {
                        Log.d("REGISTER_RESPONSE", "Full response: " + response.toString());
                        if (response.has("statusCode")) {
                            int statusCode = response.getInt("statusCode");
                            Log.d("REGISTER_RESPONSE", "Status code: " + statusCode);
                            if (statusCode == 201 || statusCode == 200) {
                                Toast.makeText(RegisterActivity.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivityWithAnim(intent);
                                finish();
                            } else {
                                String errorMessage = "Đăng ký thất bại";
                                if (response.has("message")) {
                                    errorMessage = response.getString("message");
                                }
                                Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            String errorMessage = "Đăng ký thất bại";
                            if (response.has("message")) {
                                errorMessage = response.getString("message");
                            }
                            Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e("REGISTER_ERROR", "Error parsing response: " + e.getMessage());
                        Log.e("REGISTER_ERROR", "Response that caused error: " + response.toString());
                        Toast.makeText(RegisterActivity.this, "Lỗi xử lý dữ liệu", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    btnRegister.setEnabled(true);
                    Log.e("REGISTER_ERROR", "Network error: " + error.toString());
                    String errorMessage = "Lỗi đăng ký";
                    if (error.networkResponse != null) {
                        Log.e("REGISTER_ERROR", "Error status code: " + error.networkResponse.statusCode);
                        if (error.networkResponse.data != null) {
                            try {
                                String errorData = new String(error.networkResponse.data);
                                Log.e("REGISTER_ERROR", "Error response data: " + errorData);
                                JSONObject errorJson = new JSONObject(errorData);
                                if (errorJson.has("message")) {
                                    errorMessage = errorJson.getString("message");
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Log.e("REGISTER_ERROR", "Error parsing error response: " + e.getMessage());
                            }
                        }
                    }
                    Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                });
        requestQueue.add(request);
    }

    private boolean validateInput() {
        return validateInput(true);
    }

    private boolean validateInput(boolean showError) {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        boolean valid = true;
        if (name.isEmpty()) {
            if (showError) {
                tilName.setError("Vui lòng nhập họ tên");
                errorHandler.removeCallbacks(clearNameError);
                errorHandler.postDelayed(clearNameError, 3000);
            }
            valid = false;
        } else {
            tilName.setError(null);
        }
        if (email.isEmpty()) {
            if (showError) {
                tilEmail.setError("Vui lòng nhập email");
                errorHandler.removeCallbacks(clearEmailError);
                errorHandler.postDelayed(clearEmailError, 3000);
            }
            valid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            if (showError) {
                tilEmail.setError("Email không hợp lệ");
                errorHandler.removeCallbacks(clearEmailError);
                errorHandler.postDelayed(clearEmailError, 3000);
            }
            valid = false;
        } else {
            tilEmail.setError(null);
        }
        if (password.isEmpty()) {
            if (showError) {
                tilPassword.setError("Vui lòng nhập mật khẩu");
                errorHandler.removeCallbacks(clearPasswordError);
                errorHandler.postDelayed(clearPasswordError, 3000);
            }
            valid = false;
        } else if (password.length() < 6) {
            if (showError) {
                tilPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
                errorHandler.removeCallbacks(clearPasswordError);
                errorHandler.postDelayed(clearPasswordError, 3000);
            }
            valid = false;
        } else {
            tilPassword.setError(null);
        }
        if (confirmPassword.isEmpty()) {
            if (showError) {
                tilConfirmPassword.setError("Vui lòng nhập lại mật khẩu");
                errorHandler.removeCallbacks(clearConfirmError);
                errorHandler.postDelayed(clearConfirmError, 3000);
            }
            valid = false;
        } else if (!password.equals(confirmPassword)) {
            if (showError) {
                tilConfirmPassword.setError("Mật khẩu không khớp");
                errorHandler.removeCallbacks(clearConfirmError);
                errorHandler.postDelayed(clearConfirmError, 3000);
            }
            valid = false;
        } else {
            tilConfirmPassword.setError(null);
        }
        return valid;
    }

    private void startActivityWithAnim(Intent intent) {
        startActivity(intent);
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }
}
