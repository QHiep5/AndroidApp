package com.example.jobhunter.ViewModel;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.jobhunter.api.CompanyApi;
import com.example.jobhunter.api.JobApi;
import com.example.jobhunter.model.Company;
import com.example.jobhunter.model.Job;
import com.example.jobhunter.util.TokenManager;
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

    public void fetchTopCompanies(Context context) {
        String token = TokenManager.getToken(context);
        if (token == null || token.isEmpty()) {
            error.postValue("Người dùng chưa đăng nhập");
            return;
        }
        CompanyApi.getCompanies(context, token, response -> {
            Log.d("API_RESPONSE", "Top Companies Response: " + response.toString()); // In response ra logcat
            List<Company> companies = new ArrayList<>();
            try {
                JSONObject data = response.getJSONObject("data");
                JSONArray result = data.getJSONArray("result");
                for (int i = 0; i < result.length(); i++) {
                    JSONObject obj = result.getJSONObject(i);
                    Company company = new Company();
                    company.setName(obj.optString("name"));
                    company.setLogo(obj.optString("logo"));
                    String fullLogoUrl = company.getLogo();
                    if (fullLogoUrl != null) {
                        Log.d("API_LOGO_URL", "Constructed URL: " + fullLogoUrl); // DEBUG
                    }
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

    public void fetchSuggestedJobs(Context context) {
        String token = TokenManager.getToken(context);
        if (token == null || token.isEmpty()) {
            error.postValue("Người dùng chưa đăng nhập");
            return;
        }
        JobApi.getJobs(context, token, response -> {
            Log.d("API_RESPONSE", "Suggested Jobs Response: " + response.toString()); // DEBUG
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

                    JSONObject companyObj = obj.optJSONObject("company");
                    if (companyObj != null) {
                        Company company = new Company();
                        // Lấy 'logo' từ đối tượng 'company'
                        company.setLogo(companyObj.optString("logo"));
                        job.setCompany(company);
                    }
                    
                    jobs.add(job);
                }
                Log.d("VIEWMODEL_DEBUG", "Posting " + jobs.size() + " suggested jobs to LiveData."); // DEBUG
                suggestedJobs.postValue(jobs);
            } catch (Exception e) {
                Log.e("PARSE_ERROR", "Lỗi parse danh sách công việc", e); // DEBUG
                error.postValue("Lỗi parse danh sách công việc: " + e.getMessage());
            }
        }, errorListener -> {
            Log.e("API_ERROR", "Lỗi lấy danh sách công việc", errorListener); // DEBUG
            error.postValue("Lỗi lấy danh sách công việc: " + errorListener.toString());
        });
    }
}