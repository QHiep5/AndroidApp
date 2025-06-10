package com.example.jobhunter.ViewModel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.jobhunter.api.CompanyApi;
import com.example.jobhunter.model.Company;
import com.android.volley.VolleyError;
import org.json.JSONObject;
import android.util.Log;

public class CompanyDetailViewModel extends AndroidViewModel {
    private final MutableLiveData<Company> companyLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    public CompanyDetailViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<Company> getCompanyLiveData() {
        return companyLiveData;
    }

    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    public void fetchCompany(String companyId, String token) {
        CompanyApi.getCompany(getApplication(), companyId, token, response -> {
            try {
                Log.d("CompanyDetailVM", "Raw API response: " + response.toString());
                JSONObject data = response.getJSONObject("data");
                Log.d("CompanyDetailVM", "data object: " + data.toString());
                Company c = new Company();
                c.setId(data.optLong("id"));
                Log.d("CompanyDetailVM", "id: " + c.getId());
                c.setName(data.optString("name"));
                Log.d("CompanyDetailVM", "name: " + c.getName());
                c.setDescription(data.optString("description"));
                Log.d("CompanyDetailVM", "description: " + c.getDescription());
                c.setAddress(data.optString("address"));
                Log.d("CompanyDetailVM", "address: " + c.getAddress());
                c.setLogo(data.optString("logo"));
                Log.d("CompanyDetailVM", "logo: " + c.getLogo());
                c.setCreatedAt(data.optString("createdAt"));
                Log.d("CompanyDetailVM", "createdAt: " + c.getCreatedAt());
                c.setUpdatedAt(data.optString("updatedAt"));
                Log.d("CompanyDetailVM", "updatedAt: " + c.getUpdatedAt());
                c.setCreatedBy(data.optString("createdBy"));
                Log.d("CompanyDetailVM", "createdBy: " + c.getCreatedBy());
                c.setUpdatedBy(data.optString("updatedBy"));
                Log.d("CompanyDetailVM", "updatedBy: " + c.getUpdatedBy());
                companyLiveData.setValue(c);
            } catch (Exception e) {
                Log.e("CompanyDetailVM", "Exception when parsing company detail", e);
                errorLiveData.setValue("Lỗi parse chi tiết công ty");
            }
        }, error -> {
            Log.e("CompanyDetailVM", "Volley error: " + error.getMessage(), error);
            handleError(error);
        });
    }

    private void handleError(VolleyError volleyError) {
        errorLiveData.setValue(volleyError.getMessage());
    }
}
