package com.example.jobhunter.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
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

public class LoginActivity extends AppCompatActivity {
    private EditText getUsername, getPassword;
    private Button btnLogin;
    private SessionManager sessionManager;

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

        getUsername = findViewById(R.id.username_input);
        getPassword = findViewById(R.id.password_input);
        btnLogin = findViewById(R.id.login_btn);

        // Debug: Kiểm tra ánh xạ view
        if (getUsername == null || getPassword == null || btnLogin == null) {
            Toast.makeText(this, "Lỗi ánh xạ view, kiểm tra lại ID trong layout!", Toast.LENGTH_LONG).show();
            android.util.Log.e("LOGIN_DEBUG", "Lỗi ánh xạ view: getUsername=" + getUsername + ", getPassword="
                    + getPassword + ", btnLogin=" + btnLogin);
        }

        btnLogin.setOnClickListener(v -> login());
    }

    private void login() {
        String username = getUsername.getText().toString().trim();
        String password = getPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject loginData = new JSONObject();
        try {
            loginData.put("username", username);
            loginData.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi tạo dữ liệu đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        AuthApi.login(this, loginData, response -> {
            try {
                Log.d("API_RESPONSE", "Full response: " + response.toString()); // Log phản hồi API
                if (!response.has("data"))
                    return;
                JSONObject data = response.getJSONObject("data");
                if (!data.has("access_token"))
                    return;
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
                        .apply();

                // Chuyển sang MainActivity
                startActivity(new Intent(this, MainActivity.class));
                finish();
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(this, "Lỗi xử lý token hoặc vai trò", Toast.LENGTH_SHORT).show();
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
        });
    }
}