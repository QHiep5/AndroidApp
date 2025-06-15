package com.example.jobhunter.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import com.android.volley.Response;
import com.example.jobhunter.activity.SearchResultsActivity;
import com.example.jobhunter.api.JobApi;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import com.example.jobhunter.model.Skill;
import com.example.jobhunter.ViewModel.SkillViewModel;

public class SearchHelper {
    private static ArrayList<String> selectedSkillIds = new ArrayList<>();
    private static boolean[] selectedSkillsFlags;
    private static List<Skill> allSkillsList = new ArrayList<>();

    public static void initializeSkills(SkillViewModel skillViewModel, Fragment fragment, TextView tvSelectSkills, LinearLayout selectedSkillsContainer) {
        skillViewModel.getSkillsLiveData().observe(fragment.getViewLifecycleOwner(), skills -> {
            allSkillsList = skills;
            selectedSkillsFlags = new boolean[skills.size()];
            String[] skillNames = new String[skills.size()];
            for (int i = 0; i < skills.size(); i++) {
                skillNames[i] = skills.get(i).getName();
            }
            tvSelectSkills.setOnClickListener(v -> showSkillsDialog(fragment, skillNames, selectedSkillsFlags, selectedSkillIds, allSkillsList, tvSelectSkills, selectedSkillsContainer));
        });
        skillViewModel.fetchSkills();
    }

    public static ArrayList<String> getSelectedSkillIds() {
        return selectedSkillIds;
    }

    public static void resetAllData() {
        selectedSkillIds.clear();
        if (selectedSkillsFlags != null) {
            for (int i = 0; i < selectedSkillsFlags.length; i++) {
                selectedSkillsFlags[i] = false;
            }
        }
    }

    /**
     * Hàm này gom toàn bộ logic search/filter từ HomeFragment
     * @param fragment: Fragment gọi tới (HomeFragment)
     * @param rootView: View gốc của Fragment (dùng để findViewById)
     * @param cgLocation: ChipGroup chọn location
     * @param selectedSkillIds: Danh sách id kỹ năng đã chọn
     * @param sessionManager: SessionManager để lấy token
     */
    public static void handleSearch(Fragment fragment, android.view.View rootView, com.google.android.material.chip.ChipGroup cgLocation, ArrayList<String> selectedSkillIds, SessionManager sessionManager) {
        String locationValue = "";
        int checkedId = cgLocation.getCheckedChipId();
        if (checkedId != android.view.View.NO_ID) {
            Chip chip = rootView.findViewById(checkedId);
            if (chip != null) {
                switch (chip.getText().toString()) {
                    case "Hà Nội": locationValue = "HANOI"; break;
                    case "Hồ Chí Minh": locationValue = "HOCHIMINH"; break;
                    case "Đà Nẵng": locationValue = "DANANG"; break;
                    case "Other": locationValue = "OTHER"; break;
                }
            }
        }
        String token = sessionManager.getAuthToken();
        JobApi.searchJobs(
            fragment.requireContext(),
            token,
            locationValue,
            selectedSkillIds,
            response -> {
                Intent intent = new Intent(fragment.getActivity(), SearchResultsActivity.class);
                intent.putExtra("searchResultJson", response.toString());
                fragment.startActivity(intent);
            },
            error -> {
                Toast.makeText(fragment.getContext(), "Lỗi tìm kiếm: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        );
    }

    // Hàm searchJobs gốc vẫn giữ lại nếu cần dùng ở nơi khác
    public static void searchJobs(Context context, String token, String location, List<String> skillIds, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        JobApi.searchJobs(context, token, location, skillIds, listener, errorListener);
    }

    public static void showSkillsDialog(Fragment fragment, String[] skillNamesArray, boolean[] selectedSkillsFlags, ArrayList<String> selectedSkillIds, List<Skill> allSkillsList, TextView tvSelectSkills, LinearLayout selectedSkillsContainer) {
        if (skillNamesArray.length == 0) {
            Toast.makeText(fragment.getContext(), "Đang tải kỹ năng...", Toast.LENGTH_SHORT).show();
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(fragment.getContext());
        builder.setTitle("Chọn kỹ năng");
        builder.setMultiChoiceItems(skillNamesArray, selectedSkillsFlags, (dialog, i, isChecked) -> selectedSkillsFlags[i] = isChecked);
        builder.setPositiveButton("OK", (dialog, which) -> {
            selectedSkillIds.clear();
            for (int i = 0; i < selectedSkillsFlags.length; i++) {
                if (selectedSkillsFlags[i]) {
                    selectedSkillIds.add(String.valueOf(allSkillsList.get(i).getId()));
                }
            }
            updateSelectedSkillsView(selectedSkillsFlags, selectedSkillIds, allSkillsList, tvSelectSkills, selectedSkillsContainer, fragment.getContext());
        });
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
        builder.setNeutralButton("Xóa tất cả", (dialog, which) -> {
            for (int i = 0; i < selectedSkillsFlags.length; i++) selectedSkillsFlags[i] = false;
            selectedSkillIds.clear();
            updateSelectedSkillsView(selectedSkillsFlags, selectedSkillIds, allSkillsList, tvSelectSkills, selectedSkillsContainer, fragment.getContext());
        });
        builder.show();
    }

    public static void updateSelectedSkillsView(boolean[] selectedSkillsFlags, ArrayList<String> selectedSkillIds, List<Skill> allSkillsList, TextView tvSelectSkills, LinearLayout selectedSkillsContainer, Context context) {
        selectedSkillsContainer.removeAllViews();
        if (selectedSkillIds.isEmpty()) {
            tvSelectSkills.setText("Chọn kỹ năng...");
        } else {
            tvSelectSkills.setText(selectedSkillIds.size() + " kỹ năng đã chọn");
        }
        for (int i = 0; i < selectedSkillsFlags.length; i++) {
            if (selectedSkillsFlags[i]) {
                final int index = i;
                Chip chip = new Chip(context);
                chip.setText(allSkillsList.get(i).getName());
                chip.setCloseIconVisible(true);
                chip.setOnCloseIconClickListener(v -> {
                    selectedSkillsFlags[index] = false;
                    selectedSkillIds.remove(String.valueOf(allSkillsList.get(index).getId()));
                    updateSelectedSkillsView(selectedSkillsFlags, selectedSkillIds, allSkillsList, tvSelectSkills, selectedSkillsContainer, context);
                });
                selectedSkillsContainer.addView(chip);
            }
        }
    }

    public static void showFilterForm(View filterFormContainer) {
        filterFormContainer.setVisibility(View.VISIBLE);
    }

    public static void hideFilterForm(View filterFormContainer, TextView etSearch, Context context, View rootView) {
        if (context != null) {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (rootView != null) imm.hideSoftInputFromWindow(rootView.getWindowToken(), 0);
        }
        etSearch.clearFocus();
        filterFormContainer.setVisibility(View.GONE);
    }

    // Có thể mở rộng thêm các hàm search/filter khác ở đây nếu cần
} 