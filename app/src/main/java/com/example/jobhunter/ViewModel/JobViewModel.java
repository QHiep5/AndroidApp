package com.example.jobhunter.ViewModel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.android.volley.VolleyError;
import com.example.jobhunter.api.JobApi;
import com.example.jobhunter.model.Company;
import com.example.jobhunter.model.Job;
import com.example.jobhunter.model.Skill;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import android.util.Log;

public class JobViewModel extends AndroidViewModel {
    private static final String TAG = "JobViewModel";
    private final MutableLiveData<List<Job>> jobs = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public JobViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<List<Job>> getJobsLiveData() {
        return jobs;
    }

    public LiveData<String> getErrorLiveData() {
        return error;
    }

    public void fetchJobs(String token) {
        Log.d(TAG, "fetchJobs called.");
        JobApi.getJobs(getApplication(), token, response -> {
            List<Job> jobList = new ArrayList<>();
            try {
                Log.d("JobViewModel", "API Response: " + response.toString());
                JSONObject data = response.getJSONObject("data");
                JSONArray result = data.getJSONArray("result");
                for (int i = 0; i < result.length(); i++) {
                    JSONObject jobJson = result.getJSONObject(i);
                    Job job = parseJobFromJson(jobJson);
                    jobList.add(job);
                }
                jobs.setValue(jobList);
                Log.d("JobViewModel", "Successfully parsed " + jobList.size() + " jobs.");
            } catch (Exception e) {
                Log.e("JobViewModel", "Error parsing job list", e);
                error.setValue("Lỗi parse danh sách việc làm: " + e.getMessage());
            }
        }, error -> {
            Log.e("JobViewModel", "API Error", error);
            handleError(error);
        });
    }

    public void searchJobs(String token, String location, List<String> skills) {
        Log.d(TAG, "searchJobs called with location: " + location + ", skills: " + skills.toString());
        JobApi.searchJobs(getApplication(), token, location, skills, response -> {
            List<Job> jobList = new ArrayList<>();
            try {
                Log.d(TAG, "Search API Response: " + response.toString());
                JSONObject data = response.getJSONObject("data");
                JSONArray result = data.getJSONArray("result");
                for (int i = 0; i < result.length(); i++) {
                    JSONObject jobJson = result.getJSONObject(i);
                    Job job = parseJobFromJson(jobJson);
                    jobList.add(job);
                }
                jobs.setValue(jobList);
                Log.d("JobViewModel", "Successfully parsed " + jobList.size() + " jobs from search.");
            } catch (Exception e) {
                Log.e("JobViewModel", "Error parsing search results", e);
                error.setValue("Lỗi parse kết quả tìm kiếm: " + e.getMessage());
            }
        }, error -> {
            Log.e("JobViewModel", "Search API Error", error);
            handleError(error);
        });
    }

    private Job parseJobFromJson(JSONObject jobJson) throws org.json.JSONException {
        Job job = new Job();
        job.setId(jobJson.optLong("id"));
        job.setName(jobJson.optString("name"));
        job.setLocation(jobJson.optString("location"));
        job.setSalary(jobJson.optDouble("salary"));
        job.setStartDate(jobJson.optString("startDate"));

        // Parse Company
        if (jobJson.has("company") && !jobJson.isNull("company")) {
            JSONObject companyJson = jobJson.getJSONObject("company");
            Company company = new Company();
            company.setId(companyJson.optInt("id"));
            company.setName(companyJson.optString("name"));
            company.setLogo(companyJson.optString("logo"));
            job.setCompany(company);
        }

        // Parse Skills
        if (jobJson.has("skills") && !jobJson.isNull("skills")) {
            JSONArray skillsJson = jobJson.getJSONArray("skills");
            List<Skill> skillsList = new ArrayList<>();
            for (int j = 0; j < skillsJson.length(); j++) {
                JSONObject skillJson = skillsJson.getJSONObject(j);
                Skill skill = new Skill();
                skill.setId(skillJson.optLong("id"));
                skill.setName(skillJson.optString("name"));
                skillsList.add(skill);
            }
            job.setSkills(skillsList);
            Log.d("JobViewModel", "Parsed " + skillsList.size() + " skills for job: " + job.getName());
        }

        return job;
    }

    private void handleError(VolleyError volleyError) {
        String errorMessage = "Lỗi không xác định";
        if (volleyError.networkResponse != null) {
            errorMessage = "Lỗi Server " + volleyError.networkResponse.statusCode;
            try {
                String responseBody = new String(volleyError.networkResponse.data, "utf-8");
                JSONObject data = new JSONObject(responseBody);
                errorMessage += ": " + data.optString("message");
                Log.e(TAG, "API Error " + volleyError.networkResponse.statusCode + ": " + responseBody, volleyError);
            } catch (Exception e) {
                Log.e(TAG, "Error parsing error response", e);
            }
        } else {
            errorMessage = "Lỗi kết nối mạng.";
            Log.e(TAG, "Network Error (no response)", volleyError);
        }
        error.setValue(errorMessage);
    }
}