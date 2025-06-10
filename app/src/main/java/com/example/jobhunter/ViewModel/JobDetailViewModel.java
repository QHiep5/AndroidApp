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
import org.json.JSONArray;
import org.json.JSONObject;
import android.util.Log;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.android.volley.Request;
import org.json.JSONException;
import com.android.volley.NetworkResponse;

public class JobDetailViewModel extends AndroidViewModel {
    private static final String TAG = "JobDetailViewModel";
    private final MutableLiveData<Job> _jobLiveData = new MutableLiveData<>();
    public LiveData<Job> getJobLiveData() {
        return _jobLiveData;
    }

    private final MutableLiveData<String> _errorLiveData = new MutableLiveData<>();
    public LiveData<String> getErrorLiveData() {
        return _errorLiveData;
    }

    public JobDetailViewModel(@NonNull Application application) {
        super(application);
    }

    public void fetchJobById(Application application, String jobId, String token) {
        String url = ApiConfig.BASE_URL + "jobs/" + jobId;
        Log.i(TAG, "1. fetchJobById called with URL: " + url);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    Log.i(TAG, "4. API Response Received: " + response.toString());
                    try {
                        Job job = parseJob(response);
                        Log.d(TAG, "5. JSON parsed successfully into Job object: " + job.getName());
                        _jobLiveData.postValue(job);
                        Log.i(TAG, "6. LiveData updated with new Job.");
                    } catch (JSONException e) {
                        Log.e(TAG, "ERROR parsing job details JSON", e);
                        _errorLiveData.postValue("Lỗi parse chi tiết công việc: " + e.getMessage());
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
                }
        ) {
            @Override
            public java.util.Map<String, String> getHeaders() {
                java.util.Map<String, String> headers = new java.util.HashMap<>();
                String authToken = (token != null && !token.isEmpty()) ? token : "EMPTY_TOKEN";
                headers.put("Authorization", "Bearer " + authToken);
                Log.i(TAG, "2. Setting Authorization Header: Bearer " + authToken.substring(0, Math.min(10, authToken.length())) + "...");
                return headers;
            }
        };

        Log.i(TAG, "3. Adding request to Volley queue.");
        Volley.newRequestQueue(application).add(request);
    }

    private Job parseJob(JSONObject response) throws JSONException {
        Job detailedJob = new Job();
        detailedJob.setId(response.optLong("id"));
        detailedJob.setName(response.optString("name"));
        detailedJob.setLocation(response.optString("location"));
        detailedJob.setSalary(response.optDouble("salary"));
        detailedJob.setDescription(response.optString("description"));

        JSONObject companyObj = response.optJSONObject("company");
        if (companyObj != null) {
            Company company = new Company();
            company.setName(companyObj.optString("name"));
            company.setLogo(companyObj.optString("logo"));
            detailedJob.setCompany(company);
        }

        JSONArray skillsArray = response.optJSONArray("skills");
        if (skillsArray != null && skillsArray.length() > 0) {
            StringBuilder skillsBuilder = new StringBuilder();
            for (int i = 0; i < skillsArray.length(); i++) {
                skillsBuilder.append(skillsArray.getString(i));
                if (i < skillsArray.length() - 1) {
                    skillsBuilder.append(", ");
                }
            }
            detailedJob.setSkills(skillsBuilder.toString());
        } else {
            detailedJob.setSkills("N/A");
        }
        return detailedJob;
    }

    private void handleError(VolleyError volleyError) {
        _errorLiveData.setValue("Lỗi mạng hoặc server: " + volleyError.getMessage());
    }
}