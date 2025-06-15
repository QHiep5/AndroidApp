package com.example.jobhunter.utils;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import com.example.jobhunter.R;
import com.example.jobhunter.activity.CompanyManageAdminActivity;
import com.example.jobhunter.activity.CvManagementActivity;
import com.example.jobhunter.activity.JobManageActivity;
import com.example.jobhunter.activity.LoginActivity;
import com.example.jobhunter.activity.ResumeManageActivity;
import com.google.android.material.navigation.NavigationView;
import com.example.jobhunter.util.TokenManager;

public class NavigationManager {
    private static ActionBarDrawerToggle drawerToggle;
    private static DrawerLayout drawerLayout;
    private static NavigationView navigationView;
    private static FragmentManager fragmentManager;
    private static Context context;
    private static SessionManager sessionManager;
    private static Toolbar toolbar;
    private static AppCompatActivity activity;

    public static void initialize(AppCompatActivity activity, DrawerLayout drawer, NavigationView navView,
            Toolbar toolbar) {
        context = activity;
        NavigationManager.activity = activity;
        drawerLayout = drawer;
        navigationView = navView;
        fragmentManager = activity.getSupportFragmentManager();
        sessionManager = new SessionManager(activity);
        NavigationManager.toolbar = toolbar;

        // Thiết lập toolbar
        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        activity.getSupportActionBar().setHomeButtonEnabled(true);

        // Khởi tạo drawer toggle
        drawerToggle = new ActionBarDrawerToggle(
                activity,
                drawer,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);

        // Thêm listener cho drawer
        drawer.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        // Thiết lập navigation listener
        setupNavigationListener();
    }

    private static void setupNavigationListener() {
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            String userRole = sessionManager.getUserRole();

            if ("SUPER_ADMIN".equalsIgnoreCase(userRole)) {
                // Handle admin menu items
                if (id == R.id.nav_home) {
                    Toast.makeText(activity, "Trang chủ", Toast.LENGTH_SHORT).show();
                } else if (id == R.id.nav_jobs) {
                    activity.startActivity(new Intent(activity, JobManageActivity.class));
                } else if (id == R.id.nav_companies) {
                    activity.startActivity(new Intent(activity, CompanyManageAdminActivity.class));
                } else if (id == R.id.nav_resumes) {
                    activity.startActivity(new Intent(activity, ResumeManageActivity.class));
                } else if (id == R.id.nav_logout) {
                    sessionManager.clearSession();
                    Intent intent = new Intent(context, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    context.startActivity(intent);
                }
            } else {
                // Handle regular user menu items
                if (id == R.id.action_manage_cv) {
                    activity.startActivity(new Intent(activity, CvManagementActivity.class));
                } else if (id == R.id.action_logout) {
                    sessionManager.clearSession();
                    Intent intent = new Intent(context, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    context.startActivity(intent);
                }
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    public static void syncDrawerState() {
        if (drawerToggle != null) {
            drawerToggle.syncState();
        }
    }

    public static void setDrawerIndicatorEnabled(boolean enabled) {
        if (drawerToggle != null) {
            drawerToggle.setDrawerIndicatorEnabled(enabled);
        }
    }

    public static void closeDrawer() {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    public static void openDrawer() {
        if (drawerLayout != null && !drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.openDrawer(GravityCompat.START);
        }
    }

    public static boolean isDrawerOpen() {
        return drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START);
    }

    public static void updateNavigationState() {
        if (navigationView != null) {
            // Clear existing menu first
            navigationView.getMenu().clear();

            // Check if user is logged in
            if (!TokenManager.isLoggedIn(context)) {
                // If not logged in, inflate main_menu but hide CV Management
                navigationView.inflateMenu(R.menu.main_menu);
                navigationView.getMenu().findItem(R.id.action_manage_cv).setVisible(false);

                // Change logout button to login button
                navigationView.getMenu().findItem(R.id.action_logout).setTitle("Đăng nhập");
                navigationView.getMenu().findItem(R.id.action_logout).setOnMenuItemClickListener(item -> {
                    Intent intent = new Intent(context, LoginActivity.class);
                    context.startActivity(intent);
                    return true;
                });
            } else {
                // Get user role
                String userRole = sessionManager.getUserRole();

                if ("SUPER_ADMIN".equalsIgnoreCase(userRole)) {
                    // Show admin menu
                    navigationView.inflateMenu(R.menu.toolbar_nav_menu);
                } else if ("USER".equalsIgnoreCase(userRole)) {
                    // Show full main menu for regular users
                    navigationView.inflateMenu(R.menu.main_menu);
                }
            }
        }
    }
}
