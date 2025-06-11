package com.example.jobhunter.activity;

import android.os.Bundle;


import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.jobhunter.adapter.ViewpagerAdater;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.viewpager2.widget.ViewPager2;
import com.example.jobhunter.R;
import com.example.jobhunter.fragment.JobListFragment;
import com.example.jobhunter.utils.SessionManager;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private DrawerLayout drawerLayout;
    ViewPager2 mViewPager;
    BottomNavigationView mBottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        initView();
        // Load the default fragment
        if (savedInstanceState == null) {
            loadFragment(new JobListFragment());
        }
    }

    private void initView() {
        mViewPager = findViewById(R.id.view_pager);
        mBottomNavigationView = findViewById(R.id.bottom_navigation);
        drawerLayout = findViewById(R.id.drawer_layout);

        ViewpagerAdater viewpagerAdater = new ViewpagerAdater(this);
        mViewPager.setAdapter(viewpagerAdater);

        mViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                switch (position){
                    case 0:
                        mBottomNavigationView.getMenu().findItem(R.id.nav_home).setChecked(true);
                        // getSupportActionBar().setTitle("Person");
                        break;
                    case 1:
                        mBottomNavigationView.getMenu().findItem(R.id.nav_job).setChecked(true);
//                        getSupportActionBar().setTitle("Home");
                        break;
                    case 2:
                        mBottomNavigationView.getMenu().findItem(R.id.nav_company).setChecked(true);
                        // getSupportActionBar().setTitle("Setting");
                        break;
                    case 3:
                        mBottomNavigationView.getMenu().findItem(R.id.nav_profile).setChecked(true);

//                        getSupportActionBar().setTitle("Setting");
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
        // Implementation of setupDrawer method
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        transaction.addToBackStack(null);
        transaction.commit();
    }
}