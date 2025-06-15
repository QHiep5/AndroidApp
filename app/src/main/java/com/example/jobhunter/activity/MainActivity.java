package com.example.jobhunter.activity;

import android.os.Bundle;


import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.navigation.NavigationView;

import com.example.jobhunter.adapter.ViewpagerAdater;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.viewpager2.widget.ViewPager2;
import com.example.jobhunter.R;
import com.example.jobhunter.fragment.JobListFragment;
import com.example.jobhunter.utils.NavigationManager;
import com.example.jobhunter.utils.SessionManager;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private DrawerLayout drawerLayout;
    ViewPager2 mViewPager;
    BottomNavigationView mBottomNavigationView;
    private Toolbar toolbar;
    private NavigationView navView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        mViewPager = findViewById(R.id.view_pager);
        mBottomNavigationView = findViewById(R.id.bottom_navigation);
        drawerLayout = findViewById(R.id.drawer_layout);
        toolbar = findViewById(R.id.toolbar);
        navView = findViewById(R.id.nav_view);

        ViewpagerAdater viewpagerAdater = new ViewpagerAdater(this);
        mViewPager.setAdapter(viewpagerAdater);

        mViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                switch (position){
                    case 0:
                        mBottomNavigationView.getMenu().findItem(R.id.nav_home).setChecked(true);
                        break;
                    case 1:
                        mBottomNavigationView.getMenu().findItem(R.id.nav_company).setChecked(true);
                        break;
                    case 2:
                        mBottomNavigationView.getMenu().findItem(R.id.nav_job).setChecked(true);
                        break;
                    case 3:
                        mBottomNavigationView.getMenu().findItem(R.id.nav_profile).setChecked(true);
                        break;
                }
            }
        });

        mBottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                mViewPager.setCurrentItem(0);
            } else if (itemId == R.id.nav_company) {
                mViewPager.setCurrentItem(1);
            } else if (itemId == R.id.nav_job) {
                mViewPager.setCurrentItem(2);
            } else if (itemId == R.id.nav_profile) {
                mViewPager.setCurrentItem(3);
            }
            return true;
        });

        setupDrawer();
    }

    private void setupDrawer() {
        NavigationManager.initialize(
                this,
                drawerLayout,
                navView,
                toolbar
        );
        NavigationManager.updateNavigationState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        NavigationManager.syncDrawerState();
        NavigationManager.setDrawerIndicatorEnabled(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        NavigationManager.setDrawerIndicatorEnabled(false);
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.addToBackStack(null);
        transaction.commit();
    }
}