package com.example.jobhunter.activity;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.jobhunter.adapter.ViewpagerAdater;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import androidx.viewpager2.widget.ViewPager2;
import com.example.jobhunter.R;


public class MainActivity extends AppCompatActivity {
    ViewPager2 mViewPager;
    BottomNavigationView mBottomNavigationView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Ánh xạ ViewPager & BottomNavigationView
        mViewPager = findViewById(R.id.view_pager);
        mBottomNavigationView = findViewById(R.id.bottom_navigation);

//getSupportActionBar().setBackgroundDrawable(new
//        ColorDrawable(getResources().getColor(R.color.colorAccent));
        ViewpagerAdater viewpagerAdater = new ViewpagerAdater(this);
        mViewPager.setAdapter(viewpagerAdater);

        mViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                switch (position){
                    case 0:
                        mBottomNavigationView.getMenu().findItem(R.id.nav_home).setChecked(true);
//                        getSupportActionBar().setTitle("Person");
                        break;
                    case 1:
                        mBottomNavigationView.getMenu().findItem(R.id.nav_company).setChecked(true);
//                        getSupportActionBar().setTitle("Home");
                        break;
                    case 2:
                        mBottomNavigationView.getMenu().findItem(R.id.nav_job).setChecked(true);
//                        getSupportActionBar().setTitle("Setting");
                        break;
                    case 3:
                        mBottomNavigationView.getMenu().findItem(R.id.nav_profile).setChecked(true);
//                        getSupportActionBar().setTitle("Setting");
                        break;
                }
            }
        });

        mBottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    mViewPager.setCurrentItem(0);
                } else if (itemId == R.id.nav_company) {
                    mViewPager.setCurrentItem(1);
                } else if (itemId == R.id.nav_job) {
                    mViewPager.setCurrentItem(2);
                }
                else if (itemId == R.id.nav_profile) {
                    mViewPager.setCurrentItem(3);
                }
                return true;
            }
        });
    }

}