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
import com.example.jobhunter.activity.CvManagementActivity;
import com.example.jobhunter.activity.JobManageActivity;
import com.example.jobhunter.activity.LoginActivity;
import com.example.jobhunter.activity.ResumeManageActivity;
import com.google.android.material.navigation.NavigationView;

public class NavigationManager {

    public static void setupNavigationMenu(AppCompatActivity activity, NavigationView navView, DrawerLayout drawerLayout) {
        if (activity == null || navView == null || drawerLayout == null) {
            return;
        }

        SessionManager sessionManager = new SessionManager(activity);
        String userRole = sessionManager.getUserRole();

        // Clear existing menu first
        navView.getMenu().clear();

        // Set the appropriate menu based on user role
        if ("SUPER_ADMIN".equalsIgnoreCase(userRole)) {
            navView.inflateMenu(R.menu.toolbar_nav_menu_admin);
        } else {
            navView.inflateMenu(R.menu.toolbar_nav_menu);
        }

        // Set up navigation item click listener
        navView.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            
            if (itemId == R.id.action_manage_cv) {
                activity.startActivity(new Intent(activity, CvManagementActivity.class));

            } else if (itemId == R.id.action_manage_company) {
                // TODO: Implement company management activity
                activity.startActivity(new Intent(activity, CompanyManageAdminActivity.class));

            } else if (itemId == R.id.action_manage_jobs) {
                activity.startActivity(new Intent(activity, JobManageActivity.class));
            } else if (itemId == R.id.action_manage_applications) {
                // TODO: Implement application management activity
                Toast.makeText(activity, "Quản lí đơn ứng tuyển", Toast.LENGTH_SHORT).show();
                Toast.makeText(activity, "Quản lí công ty", Toast.LENGTH_SHORT).show();
            } else if (itemId == R.id.action_manage_jobs) {
                activity.startActivity(new Intent(activity, JobManageActivity.class));
            } else if (itemId == R.id.action_manage_applications) {
                activity.startActivity(new Intent(activity, ResumeManageActivity.class));

            } else if (itemId == R.id.action_settings) {
                // TODO: Implement settings activity
                Toast.makeText(activity, "Cài đặt", Toast.LENGTH_SHORT).show();
            } else if (itemId == R.id.action_logout) {
                // Clear session and redirect to login
                sessionManager.clearSession();
                Intent intent = new Intent(activity, LoginActivity.class);
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