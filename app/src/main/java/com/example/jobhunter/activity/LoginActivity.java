package com.example.jobhunter.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.TimeoutError;
import com.android.volley.NoConnectionError;
import com.example.jobhunter.R;
import com.example.jobhunter.api.AuthApi;
import com.example.jobhunter.api.UserApi;
import com.example.jobhunter.util.TokenManager;
import com.example.jobhunter.utils.SessionManager;
import org.json.JSONException;
import org.json.JSONObject;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {
    private TextInputLayout tilLoginEmail, tilLoginPassword;
    private TextInputEditText etLoginEmail, etLoginPassword;
    private Button btnLogin;
    private SessionManager sessionManager;
    private ProgressBar progressBar;
    private Handler errorHandler = new Handler();
    private Runnable clearEmailError, clearPasswordError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sessionManager = new SessionManager(this);

        // Kiểm tra nếu đã đăng nhập thì chuyển sang MainActivity
        String token = TokenManager.getToken(this);
        if (token != null) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        tilLoginEmail = findViewById(R.id.tilLoginEmail);
        tilLoginPassword = findViewById(R.id.tilLoginPassword);
        etLoginEmail = findViewById(R.id.etLoginEmail);
        etLoginPassword = findViewById(R.id.etLoginPassword);
        btnLogin = findViewById(R.id.login_btn);
        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleLarge);
        addContentView(progressBar, new android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.WRAP_CONTENT,
                android.widget.FrameLayout.LayoutParams.WRAP_CONTENT,
                android.view.Gravity.CENTER));
        progressBar.setVisibility(View.GONE);

        // Debug: Kiểm tra ánh xạ view
        if (tilLoginEmail == null || tilLoginPassword == null || etLoginEmail == null || etLoginPassword == null
                || btnLogin == null) {
            Toast.makeText(this, "Lỗi ánh xạ view, kiểm tra lại ID trong layout!", Toast.LENGTH_LONG).show();
            android.util.Log.e("LOGIN_DEBUG", "Lỗi ánh xạ view: tilLoginEmail=" + tilLoginEmail + ", tilLoginPassword="
                    + tilLoginPassword + ", etLoginEmail=" + etLoginEmail + ", etLoginPassword=" + etLoginPassword
                    + ", btnLogin=" + btnLogin);
        }

        // Setup real-time validation
        setupRealTimeValidation();

        btnLogin.setOnClickListener(v -> {
            if (validateInput(true)) {
                login();
            }
        });

        // Thêm sự kiện chuyển đến trang đăng ký
        TextView tvRegister = findViewById(R.id.tv_register);
        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });

        // Thêm sự kiện cho nút guest
        TextView tvGuest = findViewById(R.id.tv_guest);
        tvGuest.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void setupRealTimeValidation() {
        // Clear error handlers
        clearEmailError = () -> tilLoginEmail.setError(null);
        clearPasswordError = () -> tilLoginPassword.setError(null);

        // Email validation
        etLoginEmail.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                validateEmail();
            }
        });

        // Password validation
        etLoginPassword.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                validatePassword();
            }
        });
    }

    private boolean validateEmail() {
        String email = etLoginEmail.getText().toString().trim();
        if (email.isEmpty()) {
            tilLoginEmail.setError("Vui lòng nhập email");
            errorHandler.removeCallbacks(clearEmailError);
            errorHandler.postDelayed(clearEmailError, 3000);
            return false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilLoginEmail.setError("Email không hợp lệ");
            errorHandler.removeCallbacks(clearEmailError);
            errorHandler.postDelayed(clearEmailError, 3000);
            return false;
        }
        tilLoginEmail.setError(null);
        return true;
    }

    private boolean validatePassword() {
        String password = etLoginPassword.getText().toString().trim();
        if (password.isEmpty()) {
            tilLoginPassword.setError("Vui lòng nhập mật khẩu");
            errorHandler.removeCallbacks(clearPasswordError);
            errorHandler.postDelayed(clearPasswordError, 3000);
            return false;
        } else if (password.length() < 6) {
            tilLoginPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
            errorHandler.removeCallbacks(clearPasswordError);
            errorHandler.postDelayed(clearPasswordError, 3000);
            return false;
        }
        tilLoginPassword.setError(null);
        return true;
    }

    private boolean validateInput(boolean showError) {
        boolean isValid = true;
        if (!validateEmail())
            isValid = false;
        if (!validatePassword())
            isValid = false;
        return isValid;
    }

    private void login() {
        String username = etLoginEmail.getText().toString().trim();
        String password = etLoginPassword.getText().toString().trim();

        // Show loading
        progressBar.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);

        JSONObject loginData = new JSONObject();
        try {
            loginData.put("username", username);
            loginData.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi tạo dữ liệu đăng nhập", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            btnLogin.setEnabled(true);
            return;
        }

        AuthApi.login(this, loginData, response -> {
            try {
                Log.d("API_RESPONSE", "Full response: " + response.toString());
                if (!response.has("data")) {
                    Toast.makeText(this, "Lỗi đăng nhập: Không có dữ liệu phản hồi", Toast.LENGTH_SHORT).show();
                    return;
                }
                JSONObject data = response.getJSONObject("data");
                if (!data.has("access_token")) {
                    Toast.makeText(this, "Lỗi đăng nhập: Không có token", Toast.LENGTH_SHORT).show();
                    return;
                }
                String token = data.getString("access_token");
                TokenManager.saveToken(this, token);
                sessionManager.saveAuthToken(token);

                // Lấy thông tin user từ response
                JSONObject user = data.getJSONObject("user");
                int userId = user.optInt("id");
                String userName = user.optString("name");
                String userEmail = user.optString("email");

                // Lấy tên role từ đối tượng role
                String userRoleName = "USER"; // Giá trị mặc định
                if (user.has("role")) {
                    Object roleObj = user.get("role");
                    Log.d("LOGIN_DEBUG", "Role object: " + roleObj.toString()); // Log role
                    if (roleObj instanceof JSONObject) {
                        userRoleName = ((JSONObject) roleObj).optString("name", "USER");
                    } else if (roleObj instanceof String) {
                        try {
                            JSONObject roleJson = new JSONObject((String) roleObj);
                            userRoleName = roleJson.optString("name", "USER");
                        } catch (JSONException e) {
                            Log.e("LOGIN_DEBUG", "Error parsing role JSON: " + e.getMessage());
                            userRoleName = "USER"; // Dùng giá trị mặc định nếu parse thất bại
                        }
                    }
                }
                Log.d("LOGIN_DEBUG", "User role name: " + userRoleName); // Log giá trị role đã parse
                sessionManager.saveUserRole(userRoleName);

                // Lưu thông tin user vào SharedPreferences
                SharedPreferences prefs = getSharedPreferences("jobhunter_prefs", MODE_PRIVATE);
                prefs.edit()
                        .putInt("user_id", userId)
                        .putString("user_name", userName)
                        .putString("user_email", userEmail)
                        .putString("user_role",userRoleName)
                        .apply();

                // Chuyển đến MainActivity
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(this, "Lỗi xử lý dữ liệu đăng nhập", Toast.LENGTH_SHORT).show();
            } finally {
                progressBar.setVisibility(View.GONE);
                btnLogin.setEnabled(true);
            }
        }, error -> {
            String errorMessage = "Lỗi đăng nhập";
            if (error instanceof TimeoutError) {
                errorMessage = "Hết thời gian kết nối";
            } else if (error instanceof NoConnectionError) {
                errorMessage = "Không có kết nối mạng";
            } else if (error.networkResponse != null && error.networkResponse.data != null) {
                try {
                    JSONObject errorJson = new JSONObject(new String(error.networkResponse.data));
                    if (errorJson.has("message")) {
                        errorMessage = errorJson.getString("message");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            btnLogin.setEnabled(true);
        });
    }
}