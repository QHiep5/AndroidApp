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
        Log.d("LOGIN_DEBUG", "Bắt đầu phương thức login()");

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

        Log.d("LOGIN_DEBUG", "Chuẩn bị gọi AuthApi.login()");

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
            // -- DEBUG: Cải thiện xử lý lỗi đăng nhập --
            String errorMessage = "Đăng nhập thất bại. Vui lòng thử lại."; // Thông báo mặc định
            if (error.networkResponse != null && error.networkResponse.data != null) {
                try {
                    String errorData = new String(error.networkResponse.data);
                    JSONObject errorJson = new JSONObject(errorData);
                    // Giả sử API trả về lỗi trong trường "message" hoặc "error"
                    if (errorJson.has("message")) {
                        errorMessage = errorJson.getString("message");
                    } else if (errorJson.has("error")) {
                        errorMessage = errorJson.getString("error");
                    }
                    // Ghi log chi tiết về lỗi từ server
                    Log.e("LOGIN_API_ERROR", "Status Code: " + error.networkResponse.statusCode);
                    Log.e("LOGIN_API_ERROR", "Response Data: " + errorData);
                } catch (JSONException e) {
                    // Lỗi khi phân tích JSON từ phản hồi lỗi
                    Log.e("LOGIN_JSON_ERROR", "Không thể phân tích JSON lỗi: " + new String(error.networkResponse.data));
                }
            } else if (error instanceof NoConnectionError) {
                errorMessage = "Không có kết nối mạng. Vui lòng kiểm tra lại.";
                Log.e("LOGIN_VOLLEY_ERROR", "NoConnectionError: " + error.toString());
            } else if (error instanceof TimeoutError) {
                errorMessage = "Hết thời gian chờ. Máy chủ không phản hồi.";
                Log.e("LOGIN_VOLLEY_ERROR", "TimeoutError: " + error.toString());
            } else {
                // Các lỗi Volley khác
                Log.e("LOGIN_VOLLEY_ERROR", "Lỗi Volley không xác định: " + error.toString());
            }
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        });
    }
}