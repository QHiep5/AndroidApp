package com.example.jobhunter.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.example.jobhunter.fragment.CompanyListFragment;
import com.example.jobhunter.fragment.HomeFragment;
import com.example.jobhunter.fragment.JobListFragment;
import com.example.jobhunter.fragment.ProfileFragment;

public class ViewpagerAdater extends FragmentStatePagerAdapter {
    public ViewpagerAdater(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
    }
    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new HomeFragment();
            case 1:
                return new CompanyListFragment();
            case 2:
                return new JobListFragment();
            case 3:
                return new ProfileFragment();

        }
        return null;
    }
    @Override
    public int getCount() {
        return 4;
    }
}
