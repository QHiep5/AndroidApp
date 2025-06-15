package com.example.jobhunter.ViewModel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.android.volley.VolleyError;
import com.example.jobhunter.api.ResumeApi;
import com.example.jobhunter.model.Resume;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.List;

public class ResumeViewModel extends AndroidViewModel {

    private final MutableLiveData<List<Resume>> resumesLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final Gson gson = new Gson();
    private static final String TAG = "ResumeViewModel";

    public ResumeViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<List<Resume>> getResumesLiveData() {
        return resumesLiveData;
    }

    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    public void getResumesByUser(String token) {
        ResumeApi.getResumesByUser(getApplication(), token, response -> {
            try {
                Log.d(TAG, "JSON từ server trả về: " + response.toString());

                JSONObject dataObject = response.getJSONObject("data");
                Log.d(TAG, "JSON 'data': " + dataObject.toString());

                JSONArray resultArray = dataObject.getJSONArray("result");
                Log.d(TAG, "JSON 'result': " + resultArray.toString());

                List<Resume> resumeList = new java.util.ArrayList<>();

                for (int i = 0; i < resultArray.length(); i++) {
                    JSONObject resumeObj = resultArray.getJSONObject(i);
                    Resume resume = gson.fromJson(resumeObj.toString(), Resume.class);

                    // ✅ Thêm companyName vào Job
                    if (resume.getJob() != null && resumeObj.has("companyName")) {
                        String companyName = resumeObj.getString("companyName");

                        com.example.jobhunter.model.Company company = new com.example.jobhunter.model.Company();
                        company.setName(companyName);
                        resume.getJob().setCompany(company);
                    }

                    resumeList.add(resume);
                }

                if (!resumeList.isEmpty()) {
                    Log.d(TAG, "Số lượng resume nhận được: " + resumeList.size());
                    resumesLiveData.postValue(resumeList);
                } else {
                    Log.d(TAG, " Không có resume nào.");
                    errorLiveData.postValue("Không có CV nào.");
                }

            } catch (Exception e) {
                Log.e(TAG, "Lỗi parse JSON", e);
                errorLiveData.postValue("Lỗi parse JSON: " + e.getMessage());
            }
        }, this::handleError);
    }



    private void handleError(VolleyError error) {
        if (error.networkResponse != null && error.networkResponse.data != null) {
            this.errorLiveData.postValue(new String(error.networkResponse.data));
        } else {
            this.errorLiveData.postValue(error.getMessage());
        }
    }

}