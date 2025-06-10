package com.example.jobhunter.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.jobhunter.fragment.CompanyListFragment;
import com.example.jobhunter.fragment.HomeFragment;
import com.example.jobhunter.fragment.JobListFragment;
import com.example.jobhunter.fragment.ProfileFragment;

public class ViewpagerAdater extends FragmentStateAdapter {
    public ViewpagerAdater(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 1:
                return new CompanyListFragment();
            case 2:
                return new JobListFragment();
            case 3:
                return new ProfileFragment();
            default:
                return new HomeFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 4;
    }
}
