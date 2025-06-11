package com.example.jobhunter.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ViewFlipper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.jobhunter.R;
import com.example.jobhunter.adapter.JobListAdapter;
import com.example.jobhunter.utils.SessionManager;
import com.example.jobhunter.ViewModel.JobViewModel;
import java.util.ArrayList;
import android.util.Log;
import android.widget.Toast;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link JobListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class JobListFragment extends Fragment {

    private static final String TAG = "JobListFragment";
    private JobViewModel jobViewModel;
    private RecyclerView rvSuggestedJobs;
    private JobListAdapter jobListAdapter;
    private ViewFlipper viewFlipper;
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private SessionManager sessionManager;

    public JobListFragment() {
        // Required empty public constructor
    }

    public static JobListFragment newInstance() {
        JobListFragment fragment = new JobListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_job_list, container, false);

        // Initialize Views
        drawerLayout = view.findViewById(R.id.drawer_layout);
        toolbar = view.findViewById(R.id.toolbar);
        viewFlipper = view.findViewById(R.id.viewFlipper);
        rvSuggestedJobs = view.findViewById(R.id.rv_suggested_jobs);

        // Setup RecyclerView
        rvSuggestedJobs.setLayoutManager(new LinearLayoutManager(getContext()));
        jobListAdapter = new JobListAdapter(getContext(),new ArrayList<>());
        rvSuggestedJobs.setAdapter(jobListAdapter);
        rvSuggestedJobs.setNestedScrollingEnabled(false);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Setup Toolbar and Drawer
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            activity.setSupportActionBar(toolbar);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    activity, drawerLayout, toolbar,
                    R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawerLayout.addDrawerListener(toggle);
            toggle.syncState();
        }

        // Setup ViewFlipper
        int[] banners = {R.drawable.banner_placeholder_1, R.drawable.banner_placeholder_2, R.drawable.banner_placeholder_3};
        for (int banner : banners) {
            ImageView imageView = new ImageView(getContext());
            imageView.setImageResource(banner);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            viewFlipper.addView(imageView);
        }
        viewFlipper.setFlipInterval(3000);
        viewFlipper.setAutoStart(true);
        viewFlipper.setInAnimation(getContext(), android.R.anim.fade_in);
        viewFlipper.setOutAnimation(getContext(), android.R.anim.fade_out);

        // Setup ViewModel and Observe Data
        jobViewModel = new ViewModelProvider(this).get(JobViewModel.class);
        Log.d(TAG, "ViewModel and SessionManager initialized.");

        jobViewModel.getJobsLiveData().observe(getViewLifecycleOwner(), jobs -> {
            if (jobs != null) {
                Log.d(TAG, "Jobs LiveData updated. Received " + jobs.size() + " jobs. Submitting to adapter.");
                jobListAdapter.setData(jobs);
            } else {
                Log.w(TAG, "Jobs LiveData updated with null list.");
            }
        });

        jobViewModel.getErrorLiveData().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Log.e(TAG, "Error LiveData updated: " + error);
                Toast.makeText(getContext(), "Lỗi: " + error, Toast.LENGTH_LONG).show();
            }
        });

        sessionManager = new SessionManager(getContext());
        String token = sessionManager.getAuthToken();
        if (token == null || token.isEmpty()) {
            Log.w(TAG, "Authentication token is NULL or EMPTY. Fetching jobs without auth.");
        } else {
            Log.i(TAG, "Authentication token found. Fetching jobs with token.");
        }
        jobViewModel.fetchJobs(token);
    }
}