package com.example.jobhunter.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.example.jobhunter.R;
import com.example.jobhunter.api.AuthApi;
import com.example.jobhunter.api.UserApi;
import com.example.jobhunter.util.TokenManager;
import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {
    private EditText getUsername, getPassword;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

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

        // Debug: Kiểm tra dữ liệu nhập vào
        android.util.Log.d("LOGIN_DEBUG", "Username: " + username + ", Password: " + password);
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ tài khoản và mật khẩu", Toast.LENGTH_SHORT).show();
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
                if (!response.has("data"))
                    return;
                JSONObject data = response.getJSONObject("data");
                if (!data.has("access_token"))
                    return;
                String token = data.getString("access_token");
                TokenManager.saveToken(this, token);

                // Lấy thông tin user từ response
                JSONObject user = data.getJSONObject("user");
                int userId = user.optInt("id");
                String userName = user.optString("name");
                String userEmail = user.optString("email");

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
                Toast.makeText(this, "Lỗi xử lý token", Toast.LENGTH_SHORT).show();
            }
        }, error -> {
            // Debug: In lỗi ra log
            if (error.networkResponse != null) {
                android.util.Log.e("LOGIN_ERROR", "Status: " + error.networkResponse.statusCode);
                android.util.Log.e("LOGIN_ERROR", "Data: " + new String(error.networkResponse.data));
            } else {
                android.util.Log.e("LOGIN_ERROR", "VolleyError: " + error.toString());
            }
            Toast.makeText(this, "Đăng nhập thất bại", Toast.LENGTH_SHORT).show();
        });
    }
}