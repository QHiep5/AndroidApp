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
    private final MutableLiveData<Boolean> updateResultLiveData = new MutableLiveData<>();
    private final MutableLiveData<Integer> currentPageLiveData = new MutableLiveData<>(1);
    private final MutableLiveData<Integer> totalPagesLiveData = new MutableLiveData<>(1);
    private final Gson gson = new Gson();
    private static final String TAG = "ResumeViewModel";
    private static final int ITEMS_PER_PAGE = 10;

    public ResumeViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<List<Resume>> getResumesLiveData() {
        return resumesLiveData;
    }

    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    public LiveData<Boolean> getUpdateResultLiveData() {
        return updateResultLiveData;
    }

    public LiveData<Integer> getCurrentPageLiveData() {
        return currentPageLiveData;
    }

    public LiveData<Integer> getTotalPagesLiveData() {
        return totalPagesLiveData;
    }

    public void getResumesByUser(String token) {
        int currentPage = currentPageLiveData.getValue() != null ? currentPageLiveData.getValue() : 1;
        ResumeApi.getResumesByUser(getApplication(), token, currentPage, ITEMS_PER_PAGE, response -> {
            try {
                JSONObject dataObject = response.getJSONObject("data");
                Log.d(TAG, "JSON 'data': " + dataObject.toString());

                JSONObject metaObject = dataObject.getJSONObject("meta");
                int totalItems = metaObject.getInt("total");
                Log.d(TAG, "Total items: " + totalItems);

                JSONArray resultArray = dataObject.getJSONArray("result");
                Log.d(TAG, "JSON 'result': " + resultArray.toString());

                int totalPages = (int) Math.ceil((double) totalItems / ITEMS_PER_PAGE);
                totalPagesLiveData.postValue(totalPages);

                List<Resume> resumeList = new java.util.ArrayList<>();

                for (int i = 0; i < resultArray.length(); i++) {
                    JSONObject resumeObj = resultArray.getJSONObject(i);
                    Resume resume = gson.fromJson(resumeObj.toString(), Resume.class);

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

    public void setCurrentPage(int page) {
        currentPageLiveData.setValue(page);
    }

    public void nextPage() {
        Integer currentPage = currentPageLiveData.getValue();
        Integer totalPages = totalPagesLiveData.getValue();
        if (currentPage != null && totalPages != null && currentPage < totalPages) {
            currentPageLiveData.setValue(currentPage + 1);
        }
    }

    public void previousPage() {
        Integer currentPage = currentPageLiveData.getValue();
        if (currentPage != null && currentPage > 1) {
            currentPageLiveData.setValue(currentPage - 1);
        }
    }

    public void fetchAllResumes(String token) {
        int currentPage = currentPageLiveData.getValue() != null ? currentPageLiveData.getValue() : 1;
        ResumeApi.fetchAllResumes(getApplication(), token, currentPage, ITEMS_PER_PAGE, response -> {
            try {
                JSONObject dataObject = response.getJSONObject("data");
                Log.d(TAG, "JSON 'data': " + dataObject.toString());

                JSONObject metaObject = dataObject.getJSONObject("meta");
                int totalItems = metaObject.getInt("total");
                Log.d(TAG, "Total items: " + totalItems);

                JSONArray resultArray = dataObject.getJSONArray("result");
                Log.d(TAG, "JSON 'result': " + resultArray.toString());


                int totalPages = (int) Math.ceil((double) totalItems / ITEMS_PER_PAGE);
                totalPagesLiveData.postValue(totalPages);

                List<Resume> resumeList = new java.util.ArrayList<>();
                for (int i = 0; i < resultArray.length(); i++) {
                    JSONObject resumeObj = resultArray.getJSONObject(i);
                    Resume resume = gson.fromJson(resumeObj.toString(), Resume.class);
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
                    Log.d(TAG, "Không có resume nào.");
                    errorLiveData.postValue("Không có đơn ứng tuyển nào.");
                }
            } catch (Exception e) {
                Log.e(TAG, "Lỗi parse JSON fetchAllResumes", e);
                errorLiveData.postValue("Lỗi parse JSON: " + e.getMessage());
            }
        }, this::handleError);
    }

    public void updateResumeState(long resumeId, com.example.jobhunter.util.constant.ResumeStateEnum newState, String token) {
        Log.d(TAG, "Gọi updateResumeState với resumeId: " + resumeId + ", newState: " + newState);
        ResumeApi.updateResumeState(getApplication(), resumeId, newState, token, response -> {
            Log.d(TAG, "Update resume thành công: " + response.toString());
            updateResultLiveData.postValue(true);
        }, error -> {
            Log.e(TAG, "Update resume thất bại", error);
            if (error.networkResponse != null) {
                int statusCode = error.networkResponse.statusCode;
                String responseBody = error.networkResponse.data != null ? new String(error.networkResponse.data) : "";
                Log.e(TAG, "Status code: " + statusCode + ", body: " + responseBody);
            }
            updateResultLiveData.postValue(false);
            handleError(error);
        });
    }

    private void handleError(VolleyError error) {
        if (error.networkResponse != null && error.networkResponse.data != null) {
            this.errorLiveData.postValue(new String(error.networkResponse.data));
        } else {
            this.errorLiveData.postValue(error.getMessage());
        }
    }

}
