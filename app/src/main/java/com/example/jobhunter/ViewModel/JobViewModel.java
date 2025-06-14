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
import com.example.jobhunter.util.TokenManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import android.util.Log;

public class JobViewModel extends AndroidViewModel {
    private static final String TAG = "JobViewModel";
    private final MutableLiveData<List<Job>> jobs = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<List<Job>> filteredJobs = new MutableLiveData<>();
    private final MutableLiveData<Boolean> createJobResultLiveData = new MutableLiveData<>();

    public JobViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<List<Job>> getJobsLiveData() {
        return jobs;
    }

    public LiveData<String> getErrorLiveData() {
        return error;
    }

    public LiveData<Boolean> getCreateJobResultLiveData() {
        return createJobResultLiveData;
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

    public LiveData<List<Job>> getFilteredJobs() {
        return filteredJobs;
    }

    public LiveData<String> getError() {
        return error;
    }

    public void createJob(Job job, String token) {
        JSONObject jobJson = new JSONObject();
        try {
            jobJson.put("name", job.getName());
            jobJson.put("location", job.getLocation());
            jobJson.put("salary", job.getSalary());
            jobJson.put("quantity", job.getQuantity());
            jobJson.put("level", job.getLevel().name()); // Convert Enum to String
            jobJson.put("description", job.getDescription());
            jobJson.put("startDate", job.getStartDate());
            jobJson.put("endDate", job.getEndDate());
            jobJson.put("active", job.isActive());

            // Company ID
            if (job.getCompany() != null) {
                jobJson.put("companyId", job.getCompany().getId());
            }

            // Skills IDs
            if (job.getSkills() != null && !job.getSkills().isEmpty()) {
                JSONArray skillIdsArray = new JSONArray();
                for (Skill skill : job.getSkills()) {
                    skillIdsArray.put(skill.getId());
                }
                jobJson.put("skills", skillIdsArray);
            }

            JobApi.createJob(getApplication(), jobJson,token, response -> {
                createJobResultLiveData.postValue(true);
            }, error -> {
                handleError(error);
                createJobResultLiveData.postValue(false);
            });

        } catch (JSONException e) {
            Log.e(TAG, "Error creating JSON for new job", e);
            error.postValue("Lỗi tạo dữ liệu công việc: " + e.getMessage());
            createJobResultLiveData.postValue(false);
        }
    }

    public void searchJobs(String location, List<String> skills) {
        String token = TokenManager.getToken(getApplication());
        if (token == null || token.isEmpty()) {
            error.postValue("Người dùng chưa đăng nhập");
            return;
        }

        JobApi.searchJobs(getApplication(), token, location, skills, response -> {
            Log.d(TAG, "Search Response: " + response.toString());
            List<Job> jobs = new ArrayList<>();
            try {
                JSONObject data = response.getJSONObject("data");
                JSONArray result = data.getJSONArray("result");
                for (int i = 0; i < result.length(); i++) {
                    JSONObject obj = result.getJSONObject(i);
                    Job job = new Job();
                    job.setId(obj.optInt("id"));
                    job.setName(obj.optString("name"));
                    job.setLocation(obj.optString("location"));
                    job.setSalary(obj.optDouble("salary"));
                    // Parse các trường khác nếu cần...

                    JSONObject companyObj = obj.optJSONObject("company");
                    if (companyObj != null) {
                        Company company = new Company();
                        company.setName(companyObj.optString("name"));
                        company.setLogo(companyObj.optString("logo"));
                        job.setCompany(company);
                    }
                    jobs.add(job);
                }
                filteredJobs.postValue(jobs);
            } catch (JSONException e) {
                Log.e(TAG, "Lỗi phân tích JSON khi tìm kiếm công việc", e);
                error.postValue("Lỗi phân tích dữ liệu: " + e.getMessage());
            }
        }, errorListener -> {
            Log.e(TAG, "Lỗi API khi tìm kiếm công việc", errorListener);
            error.postValue("Lỗi mạng: " + errorListener.toString());
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
        String errorMessage = "Lỗi mạng hoặc server: " + volleyError.getMessage();
        if (volleyError.networkResponse != null && volleyError.networkResponse.data != null) {
            try {
                JSONObject errorResponse = new JSONObject(new String(volleyError.networkResponse.data));
                if (errorResponse.has("message")) {
                    errorMessage = errorResponse.getString("message");
                }
            } catch (JSONException e) {
                Log.e(TAG, "Error parsing error response", e);
            }
        }
        error.postValue(errorMessage);
    }
}