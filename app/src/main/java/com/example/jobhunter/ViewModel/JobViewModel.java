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
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class JobViewModel extends AndroidViewModel {
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
        JobApi.getJobs(getApplication(), token, response -> {
            List<Job> jobList = new ArrayList<>();
            try {
                JSONObject data = response.getJSONObject("data");
                JSONArray result = data.getJSONArray("result");
                for (int i = 0; i < result.length(); i++) {
                    JSONObject obj = result.getJSONObject(i);
                    Job j = new Job();
                    j.setId(obj.optLong("id"));
                    j.setName(obj.optString("name"));
                    j.setLocation(obj.optString("location"));
                    j.setSalary(obj.optDouble("salary"));
                    // We don't parse description, company, skills here for the list view
                    // to keep it lightweight.
                    jobList.add(j);
                }
                jobs.setValue(jobList);
            } catch (Exception e) {
                error.setValue("Lỗi parse danh sách việc làm: " + e.getMessage());
            }
        }, this::handleError);
    }

    private void handleError(VolleyError volleyError) {
        error.setValue("Lỗi mạng hoặc server: " + volleyError.getMessage());
    }
}