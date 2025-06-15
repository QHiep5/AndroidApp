package com.example.jobhunter.ViewModel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.android.volley.VolleyError;
import com.example.jobhunter.api.ApiConfig;
import com.example.jobhunter.api.JobApi;
import com.example.jobhunter.model.Company;
import com.example.jobhunter.model.Job;
import com.example.jobhunter.model.Skill;
import com.example.jobhunter.util.TokenManager;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.android.volley.Request;

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
    private final MutableLiveData<Boolean> updateJobResultLiveData = new MutableLiveData<>();
    private final MutableLiveData<Job> jobByIdLiveData = new MutableLiveData<>();
    private final MutableLiveData<Job> jobForUpdateLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> _errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> deleteJobResultLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> deleteJobErrorLiveData = new MutableLiveData<>();

    // Add pagination related fields
    private final MutableLiveData<Integer> currentPage = new MutableLiveData<>(1);
    private final MutableLiveData<Integer> totalPages = new MutableLiveData<>(1);
    private static final int PAGE_SIZE = 20;

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

    public LiveData<Boolean> getUpdateJobResultLiveData() {
        return updateJobResultLiveData;
    }

    public LiveData<Job> getJobByIdLiveData() {
        return jobByIdLiveData;
    }

    public LiveData<Job> getJobForUpdateLiveData() {
        return jobForUpdateLiveData;
    }

    public LiveData<Boolean> getDeleteJobResultLiveData() {
        return deleteJobResultLiveData;
    }

    public LiveData<String> getDeleteJobErrorLiveData() {
        return deleteJobErrorLiveData;
    }

    public LiveData<Integer> getCurrentPage() {
        return currentPage;
    }

    public LiveData<Integer> getTotalPages() {
        return totalPages;
    }

    public void fetchJobs(String token) {
        fetchJobs(token, currentPage.getValue());
    }

    public void fetchJobs(String token, int page) {
        JobApi.getJobs(getApplication(), token, page, PAGE_SIZE, response -> {
            List<Job> jobList = new ArrayList<>();
            try {
                JSONObject data = response.getJSONObject("data");
                JSONObject meta = data.getJSONObject("meta");
                JSONArray result = data.getJSONArray("result");
                
                // Update pagination info
                currentPage.setValue(meta.getInt("page"));
                totalPages.setValue(meta.getInt("pages"));
                
                for (int i = 0; i < result.length(); i++) {
                    JSONObject obj = result.getJSONObject(i);
                    JSONObject jobJson = result.getJSONObject(i);
                    Job job = parseJobFromJson(jobJson);
                    jobList.add(job);
                }
                jobs.setValue(jobList);
            } catch (Exception e) {
                error.setValue("Lỗi parse danh sách việc làm");
            }
        }, this::handleError);
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
            
            // Company Object (gửi đầy đủ id, name, logo)
            if (job.getCompany() != null) {
                JSONObject companyObject = new JSONObject();
                companyObject.put("id", String.valueOf(job.getCompany().getId())); // Chuyển ID thành String
                companyObject.put("name", job.getCompany().getName()); // Thêm tên công ty
                companyObject.put("logo", job.getCompany().getLogo()); // Thêm logo công ty
                jobJson.put("company", companyObject);
            }

            // Skills IDs
            if (job.getSkills() != null && !job.getSkills().isEmpty()) {
                JSONArray skillObjectsArray = new JSONArray();
                for (Skill skill : job.getSkills()) {
                    JSONObject skillIdObject = new JSONObject();
                    skillIdObject.put("id", skill.getId());
                    skillObjectsArray.put(skillIdObject);
                }
                jobJson.put("skills", skillObjectsArray);
            }

            JobApi.createJob(getApplication(), jobJson, token, response -> {
                createJobResultLiveData.postValue(true);
            }, error -> {
                handleError(error);
                createJobResultLiveData.postValue(false);
            });

        } catch (JSONException e) {
            Log.e(TAG, "Error creating error for new job", e);
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

    public void updateJob(Job job, String token) {
        JSONObject jobJson = new JSONObject();
        try {
            jobJson.put("id", job.getId());
            jobJson.put("name", job.getName());
            jobJson.put("location", job.getLocation());
            jobJson.put("salary", job.getSalary());
            jobJson.put("quantity", job.getQuantity());
            jobJson.put("level", job.getLevel().name());
            jobJson.put("description", job.getDescription());
            jobJson.put("startDate", job.getStartDate());
            jobJson.put("endDate", job.getEndDate());
            jobJson.put("active", job.isActive());

            // Company Object (gửi đầy đủ id, name, logo)
            if (job.getCompany() != null) {
                JSONObject companyObject = new JSONObject();
                companyObject.put("id", String.valueOf(job.getCompany().getId()));
                companyObject.put("name", job.getCompany().getName());
                companyObject.put("logo", job.getCompany().getLogo());
                jobJson.put("company", companyObject);
            }

            // Skills IDs
            if (job.getSkills() != null && !job.getSkills().isEmpty()) {
                JSONArray skillObjectsArray = new JSONArray();
                for (Skill skill : job.getSkills()) {
                    JSONObject skillIdObject = new JSONObject();
                    skillIdObject.put("id", skill.getId());
                    skillObjectsArray.put(skillIdObject);
                }
                jobJson.put("skills", skillObjectsArray);
            }

            JobApi.updateJob(getApplication(), jobJson, token, response -> {
                updateJobResultLiveData.postValue(true);
            }, error -> {
                handleError(error);
                updateJobResultLiveData.postValue(false);
            });
        } catch (JSONException e) {
            Log.e(TAG, "Error creating error for update job", e);
            error.postValue("Lỗi tạo dữ liệu cập nhật công việc: " + e.getMessage());
            updateJobResultLiveData.postValue(false);
        }
    }

    public void fetchJobByIdForUpdate(Application application, String jobId, String token) {
        String url = ApiConfig.BASE_URL + "jobs/" + jobId;
        Log.i(TAG, "fetchJobByIdForUpdate called with URL: " + url);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    Log.i(TAG, "API Response Received: " + response.toString());
                    try {
                        JSONObject dataObj = response.optJSONObject("data");
                        if (dataObj != null) {
                            Job job = parseJobForUpdate(dataObj);
                            jobForUpdateLiveData.postValue(job);
                            Log.i(TAG, "LiveData updated with new Job for update.");
                        } else {
                            Log.e(TAG, "ERROR: data object is null in response");
                            error.postValue("Lỗi: Không có dữ liệu công việc trong phản hồi.");
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "ERROR parsing job details JSON", e);
                        error.postValue("Lỗi parse chi tiết công việc: " + e.getMessage());
                    }
                },
                error -> {
                    String errorMessage = "Lỗi khi tải dữ liệu công việc";
                    if (error.networkResponse != null) {
                        int statusCode = error.networkResponse.statusCode;
                        errorMessage += " (Error " + statusCode + ")";
                        Log.e(TAG, "API Network Error. Status Code: " + statusCode, error);
                    } else {
                        Log.e(TAG, "API Volley Error (no network response)", error);
                    }
                    _errorLiveData.postValue(errorMessage);
                });

        Log.i(TAG, "Adding request to Volley queue.");
        Volley.newRequestQueue(application).add(request);
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

    private Job parseJobForUpdate(JSONObject jobJson) throws org.json.JSONException {
        Job job = new Job();
        job.setId(jobJson.optLong("id"));
        job.setName(jobJson.optString("name"));
        job.setLocation(jobJson.optString("location"));
        job.setSalary(jobJson.optDouble("salary"));
        job.setQuantity(jobJson.optInt("quantity"));
        job.setLevel(com.example.jobhunter.util.constant.LevelEnum.valueOf(jobJson.optString("level")));
        job.setDescription(jobJson.optString("description"));
        job.setStartDate(jobJson.optString("startDate"));
        job.setEndDate(jobJson.optString("endDate"));
        job.setActive(jobJson.optBoolean("active"));
        // Parse Company
        if (jobJson.has("company") && !jobJson.isNull("company")) {
            JSONObject companyJson = jobJson.getJSONObject("company");
            com.example.jobhunter.model.Company company = new com.example.jobhunter.model.Company();
            company.setId(companyJson.optLong("id"));
            company.setName(companyJson.optString("name"));
            company.setLogo(companyJson.optString("logo"));
            job.setCompany(company);
        }
        // Parse Skills
        if (jobJson.has("skills") && !jobJson.isNull("skills")) {
            JSONArray skillsJson = jobJson.getJSONArray("skills");
            List<com.example.jobhunter.model.Skill> skillsList = new ArrayList<>();
            for (int j = 0; j < skillsJson.length(); j++) {
                JSONObject skillJson = skillsJson.getJSONObject(j);
                com.example.jobhunter.model.Skill skill = new com.example.jobhunter.model.Skill();
                skill.setId(skillJson.optLong("id"));
                skill.setName(skillJson.optString("name"));
                skillsList.add(skill);
            }
            job.setSkills(skillsList);
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

    public void deleteJob(long jobId, String token) {
        JobApi.deleteJob(getApplication(), jobId, token, response -> {
            deleteJobResultLiveData.postValue(true);
            deleteJobErrorLiveData.postValue("");
            fetchJobs(token);
        }, error -> {
            deleteJobResultLiveData.postValue(false);
            handleError(error);
            String errorMsg = "";
            if (error != null && error.networkResponse != null && error.networkResponse.data != null) {
                errorMsg = new String(error.networkResponse.data);
                if (!errorMsg.trim().isEmpty()) {
                    try {
                        org.json.JSONObject obj = new org.json.JSONObject(errorMsg);
                        if (obj.has("message")) errorMsg = obj.getString("message");
                    } catch (Exception ignored) {}
                } else {
                    errorMsg = "Xoá thất bại, không nhận được phản hồi từ máy chủ.";
                }
            } else if (error != null && error.getMessage() != null) {
                errorMsg = error.getMessage();
        }
            deleteJobErrorLiveData.postValue(errorMsg);
            android.util.Log.e(TAG, "Xoá job thất bại: " + errorMsg, error);
        });
    }
}