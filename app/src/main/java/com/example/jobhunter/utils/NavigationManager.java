package com.example.jobhunter.utils;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.jobhunter.R;
import com.example.jobhunter.activity.CompanyManageAdminActivity;
import com.example.jobhunter.activity.CvManagementActivity;
import com.example.jobhunter.activity.JobManageActivity;
import com.example.jobhunter.activity.LoginActivity;
import com.example.jobhunter.activity.ResumeManageActivity;
import com.google.android.material.navigation.NavigationView;

public class NavigationManager {

    public static void setupNavigationMenu(AppCompatActivity activity, NavigationView navView,
            DrawerLayout drawerLayout) {
        if (activity == null || navView == null || drawerLayout == null) {
            return;
        }

        // Lấy thông tin user từ SharedPreferences
        android.content.SharedPreferences prefs = activity.getSharedPreferences("jobhunter_prefs",
                android.content.Context.MODE_PRIVATE);
        String userEmail = prefs.getString("user_email", null);
        String userRole = prefs.getString("user_role", "USER");
        boolean isLoggedIn = com.example.jobhunter.util.TokenManager.isLoggedIn(activity);
        android.util.Log.d("NAV_DEBUG",
                "isLoggedIn: " + isLoggedIn + ", userEmail: " + userEmail + ", userRole: " + userRole);

        navView.getMenu().clear();

        if (!isLoggedIn) {
            // Nếu chưa đăng nhập, chỉ hiển thị toolbar_nav_menu và ẩn Quản lý CV
            navView.inflateMenu(R.menu.toolbar_nav_menu);
            if (navView.getMenu().findItem(R.id.action_manage_cv) != null) {
                navView.getMenu().findItem(R.id.action_manage_cv).setVisible(false);
            }
        } else if ("SUPER_ADMIN".equalsIgnoreCase(userRole)) {
            // Nếu là SUPER_ADMIN, hiển thị menu admin
            navView.inflateMenu(R.menu.toolbar_nav_menu_admin);
        } else {
            // Nếu là user thường, hiển thị đầy đủ toolbar_nav_menu
            navView.inflateMenu(R.menu.toolbar_nav_menu);
        }

        navView.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_manage_cv) {
                if (!isLoggedIn) {
                    Toast.makeText(activity, "Bạn cần đăng nhập để sử dụng chức năng này", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(activity, com.example.jobhunter.activity.LoginActivity.class);
                    activity.startActivity(intent);
                    return true;
                }
                activity.startActivity(new Intent(activity, com.example.jobhunter.activity.CvManagementActivity.class));
            } else if (itemId == R.id.action_manage_company) {
                activity.startActivity(
                        new Intent(activity, com.example.jobhunter.activity.CompanyManageAdminActivity.class));
            } else if (itemId == R.id.action_manage_jobs) {
                activity.startActivity(new Intent(activity, com.example.jobhunter.activity.JobManageActivity.class));
            } else if (itemId == R.id.action_manage_applications) {
                activity.startActivity(new Intent(activity, com.example.jobhunter.activity.ResumeManageActivity.class));
            } else if (itemId == R.id.action_settings) {
                Toast.makeText(activity, "Cài đặt", Toast.LENGTH_SHORT).show();
            } else if (itemId == R.id.action_logout) {
                // Clear session and redirect to login
                prefs.edit().clear().apply();
                com.example.jobhunter.util.TokenManager.clearToken(activity);
                Intent intent = new Intent(activity, com.example.jobhunter.activity.LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                activity.startActivity(intent);
                activity.finish();
            } else {
                Toast.makeText(activity, "Bạn chọn: " + item.getTitle(), Toast.LENGTH_SHORT).show();
            }
            drawerLayout.closeDrawer(androidx.core.view.GravityCompat.START);
            return true;
        });
    }
}