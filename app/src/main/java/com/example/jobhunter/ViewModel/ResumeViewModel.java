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

    public void fetchResumes(String token) {
        ResumeApi.getResumes(getApplication(), token, response -> resumes.setValue(response), this::handleError);
    }

    public void fetchResume(String resumeId, String token) {
        ResumeApi.getResume(getApplication(), resumeId, token, response -> resume.setValue(response), this::handleError);
    }



    private void handleError(VolleyError error) {
        if (error.networkResponse != null && error.networkResponse.data != null) {
            this.errorLiveData.postValue(new String(error.networkResponse.data));
        } else {
            this.errorLiveData.postValue(error.getMessage());
        }
    }

}