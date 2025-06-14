package com.example.jobhunter.ViewModel;

import android.app.Application;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.LiveData;

import com.android.volley.NetworkResponse;
import com.example.jobhunter.api.CompanyApi;
import org.json.JSONArray;
import org.json.JSONObject;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.example.jobhunter.model.Company;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;

public class CompanyViewModel extends AndroidViewModel {
    private MutableLiveData<List<Company>> companies = new MutableLiveData<>();
    private MutableLiveData<JSONObject> company = new MutableLiveData<>();
    private MutableLiveData<String> error = new MutableLiveData<>();
    private MutableLiveData<String> uploadResult = new MutableLiveData<>();
    private MutableLiveData<Boolean> createResult = new MutableLiveData<>();

    public CompanyViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<List<Company>> getCompaniesLiveData() { return companies; }
    public LiveData<JSONObject> getCompanyLiveData() { return company; }
    public LiveData<String> getErrorLiveData() { return error; }
    public LiveData<String> getUploadResult() { return uploadResult; }
    public LiveData<Boolean> getCreateResult() { return createResult; }

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
//                    c.setField(obj.optString("field"));
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

    public void uploadFile(Uri fileUri,String companyName) {
        Log.d("trcvaotry","121");
        Log.d("fileUri",String.valueOf(fileUri));
        CompanyApi.uploadFile(getApplication(), fileUri,companyName,
                response -> {
                    try {
                        Log.d("UploadDebug", "Upload successful. Raw response: " + response.toString());

                        // Parse JSON response
                        JSONObject dataObject = response.getJSONObject("data");
                        String fileName = dataObject.getString("fileName");

                        Log.d("UploadDebug", "Parsed fileName: " + fileName);

                        uploadResult.setValue(fileName);
                    } catch (JSONException e) {
                        Log.e("UploadDebug", "JSON parsing error: " + e.getMessage());
                        error.setValue("Error parsing upload response");
                    }

                },
                error -> {
                    if (error instanceof VolleyError) {
                        NetworkResponse networkResponse = ((VolleyError) error).networkResponse;
                        if (networkResponse != null) {
                            String errorData = new String(networkResponse.data);
                            Log.e("UploadDebug", "Upload failed. Status Code: " + networkResponse.statusCode);
                            Log.e("UploadDebug", "Error Body: " + errorData);
                        }
                    }
                    Log.e("UploadDebug", "Upload failed: " + error.toString());
                    this.handleError(error);
                }
        );
    }

    public void createCompany(String name, String address, String description, String logo) {
        CompanyApi.createCompany(getApplication(), name, address, description, logo,
            response -> {
                try {
                    JSONObject data = response.getJSONObject("data");
                    if (data != null) {
                        createResult.setValue(true);
                    } else {
                        error.setValue("Error creating company");
                    }
                } catch (JSONException e) {
                    error.setValue("Error parsing create company response");
                }
            },
            this::handleError
        );
    }

    private void handleError(VolleyError volleyError) {
        error.setValue(volleyError.getMessage());
    }
}