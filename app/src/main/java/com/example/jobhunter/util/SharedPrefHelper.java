package com.example.jobhunter.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefHelper {

    private static final String PREF_NAME = "jobhunter_prefs";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_TOKEN = "auth_token";

    private static SharedPrefHelper instance;
    private final SharedPreferences sharedPreferences;

    // Constructor riêng tư để Singleton
    private SharedPrefHelper(Context context) {
        sharedPreferences = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // Singleton Instance
    public static synchronized SharedPrefHelper getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPrefHelper(context);
        }
        return instance;
    }

    // Lưu userId
    public void saveUserId(int userId) {
        sharedPreferences.edit().putInt(KEY_USER_ID, userId).apply();
    }

    public int getUserId() {
        return sharedPreferences.getInt(KEY_USER_ID, -1);
    }

    // Lưu token
    public void saveToken(String token) {
        sharedPreferences.edit().putString(KEY_TOKEN, token).apply();
    }

    // Lấy token từ instance
    public String getAuthToken() {
        return sharedPreferences.getString(KEY_TOKEN, null);
    }

    // Lấy token từ static (cho tiện sử dụng ngoài)
    public static String getToken(Context context) {
        return getInstance(context).getAuthToken();
    }

    // Xóa toàn bộ thông tin người dùng
    public void clearUserData() {
        sharedPreferences.edit().clear().apply();
    }
}
