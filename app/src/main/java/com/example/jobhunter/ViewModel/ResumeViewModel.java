package com.example.jobhunter.ViewModel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.LiveData;
import com.example.jobhunter.api.ResumeApi;
import org.json.JSONArray;
import org.json.JSONObject;
import com.android.volley.Response;
import com.android.volley.VolleyError;

public class ResumeViewModel extends AndroidViewModel {
    private MutableLiveData<JSONArray> resumes = new MutableLiveData<>();
    private MutableLiveData<JSONObject> resume = new MutableLiveData<>();
    private MutableLiveData<String> error = new MutableLiveData<>();

    public ResumeViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<JSONArray> getResumesLiveData() { return resumes; }
    public LiveData<JSONObject> getResumeLiveData() { return resume; }
    public LiveData<String> getErrorLiveData() { return error; }

    public void fetchResumes(String token) {
        ResumeApi.getResumes(getApplication(), token, response -> resumes.setValue(response), this::handleError);
    }

    public void fetchResume(String resumeId, String token) {
        ResumeApi.getResume(getApplication(), resumeId, token, response -> resume.setValue(response), this::handleError);
    }

    private void handleError(VolleyError volleyError) {
        error.setValue(volleyError.getMessage());
    }
}