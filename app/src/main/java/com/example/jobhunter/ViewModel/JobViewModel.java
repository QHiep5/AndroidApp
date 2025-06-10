package com.example.jobhunter.ViewModel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.LiveData;
import com.example.jobhunter.api.JobApi;
import org.json.JSONArray;
import org.json.JSONObject;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.example.jobhunter.model.Job;
import java.util.ArrayList;
import java.util.List;

public class JobViewModel extends AndroidViewModel {
    private MutableLiveData<List<Job>> jobs = new MutableLiveData<>();
    private MutableLiveData<JSONObject> job = new MutableLiveData<>();
    private MutableLiveData<String> error = new MutableLiveData<>();

    public JobViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<List<Job>> getJobsLiveData() { return jobs; }
    public LiveData<JSONObject> getJobLiveData() { return job; }
    public LiveData<String> getErrorLiveData() { return error; }

    public void fetchJobs(String token) {
        JobApi.getJobs(getApplication(), token, response -> {
            List<Job> jobList = new ArrayList<>();
            try {
                JSONObject data = response.getJSONObject("data");
                JSONArray result = data.getJSONArray("result");
                for (int i = 0; i < result.length(); i++) {
                    JSONObject obj = result.getJSONObject(i);
                    Job j = new Job();
                    j.setName(obj.optString("name"));
                    j.setLocation(obj.optString("location"));
                    j.setSalary(obj.optDouble("salary"));
                    j.setStartDate(obj.optString("startDate"));
                    // Parse các trường khác nếu cần
                    jobList.add(j);
                }
                jobs.setValue(jobList);
            } catch (Exception e) {
                error.setValue("Lỗi parse danh sách việc làm");
            }
        }, this::handleError);
    }

    public void fetchJob(String jobId, String token) {
        JobApi.getJob(getApplication(), jobId, token, response -> job.setValue(response), this::handleError);
    }

    private void handleError(VolleyError volleyError) {
        error.setValue(volleyError.getMessage());
    }
}