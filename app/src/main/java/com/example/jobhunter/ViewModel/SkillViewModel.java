package com.example.jobhunter.ViewModel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.LiveData;
import com.example.jobhunter.api.SkillApi;
import org.json.JSONArray;
import org.json.JSONObject;
import com.android.volley.Response;
import com.android.volley.VolleyError;

public class SkillViewModel extends AndroidViewModel {
    private MutableLiveData<JSONArray> skills = new MutableLiveData<>();
    private MutableLiveData<JSONObject> skill = new MutableLiveData<>();
    private MutableLiveData<String> error = new MutableLiveData<>();

    public SkillViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<JSONArray> getSkillsLiveData() { return skills; }
    public LiveData<JSONObject> getSkillLiveData() { return skill; }
    public LiveData<String> getErrorLiveData() { return error; }

    public void fetchSkills(String token) {
        SkillApi.getSkills(getApplication(), token, response -> skills.setValue(response), this::handleError);
    }

    public void fetchSkill(String skillId, String token) {
        SkillApi.getSkill(getApplication(), skillId, token, response -> skill.setValue(response), this::handleError);
    }

    private void handleError(VolleyError volleyError) {
        error.setValue(volleyError.getMessage());
    }
}