package com.example.jobhunter.ViewModel;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.jobhunter.api.CompanyApi;
import com.example.jobhunter.api.JobApi;
import com.example.jobhunter.model.Company;
import com.example.jobhunter.model.Job;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class HomeViewModel extends ViewModel {
    private final MutableLiveData<List<Company>> topCompanies = new MutableLiveData<>();
    private final MutableLiveData<List<Job>> suggestedJobs = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public LiveData<List<Company>> getTopCompanies() { return topCompanies; }
    public LiveData<List<Job>> getSuggestedJobs() { return suggestedJobs; }
    public LiveData<String> getError() { return error; }

    public void fetchTopCompanies(Context context, String token) {
        CompanyApi.getCompanies(context, token, response -> {
            Log.d("API_RESPONSE", response.toString()); // In response ra logcat
            List<Company> companies = new ArrayList<>();
            try {
                JSONObject data = response.getJSONObject("data");
                JSONArray result = data.getJSONArray("result");
                for (int i = 0; i < result.length(); i++) {
                    JSONObject obj = result.getJSONObject(i);
                    Company company = new Company();
                    company.setName(obj.optString("name"));
                    // Parse các trường khác nếu cần
                    companies.add(company);
                }
                topCompanies.postValue(companies);
            } catch (Exception e) {
                Log.e("PARSE_ERROR", "Lỗi parse danh sách công ty", e);
                error.postValue("Lỗi parse danh sách công ty: " + e.getMessage());
            }
        }, errorListener -> {
            Log.e("API_ERROR", "Lỗi lấy danh sách công ty", errorListener);
            error.postValue("Lỗi lấy danh sách công ty: " + errorListener.toString());
        });
    }

    public void fetchSuggestedJobs(Context context, String token) {
        JobApi.getJobs(context, token, response -> {
            List<Job> jobs = new ArrayList<>();
            try {
                JSONObject data = response.getJSONObject("data");
                JSONArray result = data.getJSONArray("result");
                for (int i = 0; i < result.length(); i++) {
                    JSONObject obj = result.getJSONObject(i);
                    Job job = new Job();
                    job.setName(obj.optString("name"));
                    job.setLocation(obj.optString("location"));
                    job.setSalary(obj.optDouble("salary"));
                    job.setStartDate(obj.optString("startDate"));
                    // Parse các trường khác nếu cần
                    jobs.add(job);
                }
                suggestedJobs.postValue(jobs);
            } catch (Exception e) {
                error.postValue("Lỗi parse danh sách việc làm");
            }
        }, errorListener -> error.postValue("Lỗi lấy danh sách việc làm"));
    }
}