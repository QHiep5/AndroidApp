package com.example.jobhunter.fragment;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jobhunter.R;
import com.example.jobhunter.adapter.JobListAdapter;
import com.example.jobhunter.utils.SessionManager;
import com.example.jobhunter.ViewModel.JobViewModel;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    private JobViewModel jobViewModel;
    private SessionManager sessionManager;
    private JobListAdapter jobListAdapter;

    private Spinner spinnerLocation;
    private EditText etSkills;
    private ChipGroup chipGroupSkills;
    private Button btnSearch;
    private RecyclerView rvSearchResults;

    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = new SessionManager(requireContext());
        jobViewModel = new ViewModelProvider(this).get(JobViewModel.class);

        // Initialize views
        spinnerLocation = view.findViewById(R.id.spinner_location);
        etSkills = view.findViewById(R.id.et_skills);
        chipGroupSkills = view.findViewById(R.id.chip_group_skills);
        btnSearch = view.findViewById(R.id.btn_search);
        rvSearchResults = view.findViewById(R.id.rv_search_results);

        setupSpinner();
        setupSkillInput();
        setupRecyclerView();
        setupSearchButton();
        observeViewModel();
    }

    private void setupSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.locations_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLocation.setAdapter(adapter);
    }

    private void setupSkillInput() {
        etSkills.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                String skill = etSkills.getText().toString().trim();
                if (!skill.isEmpty()) {
                    addChipToGroup(skill);
                    etSkills.setText("");
                }
                return true;
            }
            return false;
        });
    }

    private void addChipToGroup(String skill) {
        Chip chip = new Chip(requireContext());
        chip.setText(skill);
        chip.setCloseIconVisible(true);
        chip.setClickable(true);
        chip.setCheckable(false);
        chip.setOnCloseIconClickListener(v -> chipGroupSkills.removeView(chip));
        chipGroupSkills.addView(chip);
    }

    private void setupRecyclerView() {
        jobListAdapter = new JobListAdapter(requireContext(), new ArrayList<>());
        rvSearchResults.setLayoutManager(new LinearLayoutManager(getContext()));
        rvSearchResults.setAdapter(jobListAdapter);
    }

    private void setupSearchButton() {
        btnSearch.setOnClickListener(v -> {
            String location = spinnerLocation.getSelectedItem().toString();
            List<String> skills = new ArrayList<>();
            for (int i = 0; i < chipGroupSkills.getChildCount(); i++) {
                Chip chip = (Chip) chipGroupSkills.getChildAt(i);
                skills.add(chip.getText().toString());
            }

            String token = sessionManager.getAuthToken();
            jobViewModel.searchJobs(token, location, skills);
        });
    }

    private void observeViewModel() {
        jobViewModel.getJobsLiveData().observe(getViewLifecycleOwner(), jobs -> {
            if (jobs != null) {
                jobListAdapter.setData(jobs);
                if (jobs.isEmpty()) {
                    Toast.makeText(getContext(), "Không tìm thấy công việc nào phù hợp.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        jobViewModel.getErrorLiveData().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
            }
        });
    }
}
