package com.example.jobhunter.ViewModel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.LiveData;
import com.example.jobhunter.api.CompanyApi;
import org.json.JSONArray;
import org.json.JSONObject;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.example.jobhunter.model.Company;
import java.util.ArrayList;
import java.util.List;

public class CompanyViewModel extends AndroidViewModel {
    private MutableLiveData<List<Company>> companies = new MutableLiveData<>();
    private MutableLiveData<JSONObject> company = new MutableLiveData<>();
    private MutableLiveData<String> error = new MutableLiveData<>();

    public CompanyViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<List<Company>> getCompaniesLiveData() { return companies; }
    public LiveData<JSONObject> getCompanyLiveData() { return company; }
    public LiveData<String> getErrorLiveData() { return error; }

    public void fetchCompanies(String token) {
        CompanyApi.getCompanies(getApplication(), token, response -> {
            List<Company> companyList = new ArrayList<>();
            try {
                JSONObject data = response.getJSONObject("data");
                JSONArray result = data.getJSONArray("result");
                for (int i = 0; i < result.length(); i++) {
                    JSONObject obj = result.getJSONObject(i);
                    Company c = new Company();
                    c.setId(obj.optLong("id"));
                    c.setName(obj.optString("name"));
                    c.setDescription(obj.optString("description"));
                    c.setAddress(obj.optString("address"));
                    c.setLogo(obj.optString("logo"));
                    c.setCreatedAt(obj.optString("createdAt"));
                    c.setUpdatedAt(obj.optString("updatedAt"));
                    c.setCreatedBy(obj.optString("createdBy"));
                    c.setUpdatedBy(obj.optString("updatedBy"));
                    companyList.add(c);
                }
                companies.setValue(companyList);
            } catch (Exception e) {
                error.setValue("Lỗi parse danh sách công ty");
            }
        }, this::handleError);
    }

    public void fetchCompany(String companyId, String token) {
        CompanyApi.getCompany(getApplication(), companyId, token, response -> company.setValue(response), this::handleError);
    }

    private void handleError(VolleyError volleyError) {
        error.setValue(volleyError.getMessage());
    }
}