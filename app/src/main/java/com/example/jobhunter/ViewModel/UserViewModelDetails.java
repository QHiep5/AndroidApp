package com.example.jobhunter.ViewModel;


import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.android.volley.VolleyError;
import com.example.jobhunter.api.UserApi;
import com.example.jobhunter.model.Company;
import com.example.jobhunter.model.Skill;
import com.example.jobhunter.model.User;
import com.example.jobhunter.util.constant.GenderEnum;
import com.example.jobhunter.util.constant.LevelEnum;
import com.example.jobhunter.utils.SessionManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class UserViewModelDetails extends AndroidViewModel {
    private final MutableLiveData<User> userLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> updateResultLiveData = new MutableLiveData<>();

    public UserViewModelDetails(@NonNull Application application) {
        super(application);
    }

    public LiveData<User> getUserDetails(int userId) {
        fetchUserDetails(userId);
        return userLiveData;
    }

    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    public LiveData<Boolean> getUpdateResultLiveData() {
        return updateResultLiveData;
    }

    private void fetchUserDetails(int userId) {
        Log.d("useridtrongUserViewModelDetails",String.valueOf(userId));
        SessionManager sessionManager = new SessionManager(getApplication().getApplicationContext());
        String token = sessionManager.getAuthToken();
        UserApi.getUser(getApplication().getApplicationContext(), String.valueOf(userId), token, response -> {
            try {
                JSONObject userJson = response.getJSONObject("data");
                User user = parseUserFromJson(userJson);
                userLiveData.postValue(user);
            } catch (JSONException e) {
                errorLiveData.postValue("Lỗi phân tích dữ liệu người dùng: " + e.getMessage());
            }
        }, error -> handleError(error));
    }


    private User parseUserFromJson(JSONObject userJson) throws JSONException {
        User userObj = new User();
        userObj.setId(userJson.optInt("id"));
        userObj.setName(userJson.optString("name"));
        userObj.setAge(userJson.optLong("Age"));
        userObj.setEmail(userJson.optString("email"));
        userObj.setAddress(userJson.optString("address"));
        userObj.setSalary(userJson.optDouble("salary"));
        userObj.setLevel(LevelEnum.valueOf(userJson.optString("level", "JUNIOR")));
        userObj.setGender(GenderEnum.valueOf(userJson.optString("gender", "OTHER")));
        if (userJson.has("company") && !userJson.isNull("company")) {
            JSONObject companyJson = userJson.getJSONObject("company");
            Company company = new Company();
            company.setId(companyJson.optLong("id"));
            company.setName(companyJson.optString("name"));
            userObj.setCompany(company);
        }
        if (userJson.has("skills") && !userJson.isNull("skills")) {
            JSONArray skillsJson = userJson.getJSONArray("skills");
            List<Skill> skillsList = new ArrayList<>();
            for (int i = 0; i < skillsJson.length(); i++) {
                JSONObject skillJson = skillsJson.getJSONObject(i);
                Skill skill = new Skill();
                skill.setId(skillJson.optLong("id"));
                skill.setName(skillJson.optString("name"));
                skillsList.add(skill);
            }
            userObj.setSkills(skillsList);
        }
        return userObj;
    }

    private void handleError(VolleyError volleyError) {
        if (volleyError.networkResponse != null && volleyError.networkResponse.data != null) {
            errorLiveData.postValue(new String(volleyError.networkResponse.data));
        } else {
            errorLiveData.postValue(volleyError.getMessage());
        }
    }

    public void updateUser(User user) {
        try {
            JSONObject userJson = new JSONObject();
            userJson.put("id", user.getId());
            userJson.put("name", user.getName());
            userJson.put("email", user.getEmail());
            userJson.put("age", user.getAge());
            userJson.put("gender", user.getGender() != null ? user.getGender().name() : JSONObject.NULL);
            userJson.put("address", user.getAddress());
            userJson.put("level", user.getLevel() != null ? user.getLevel().name() : JSONObject.NULL);
            userJson.put("salary", user.getSalary());

            // Handle skills array - only include skill IDs
            if (user.getSkills() != null && !user.getSkills().isEmpty()) {
                JSONArray skillsArray = new JSONArray();
                for (Skill skill : user.getSkills()) {
                    JSONObject skillJson = new JSONObject();
                    skillJson.put("id", skill.getId());
                    skillsArray.put(skillJson);
                }
                userJson.put("skills", skillsArray);
            } else {
                userJson.put("skills", new JSONArray());
            }

            SessionManager sessionManager = new SessionManager(getApplication().getApplicationContext());
            String token = sessionManager.getAuthToken();
            UserApi.updateUser(getApplication().getApplicationContext(), userJson, token, response -> {
                updateResultLiveData.postValue(true);
            }, error -> {
                updateResultLiveData.postValue(false);
            });
        } catch (Exception e) {
            updateResultLiveData.postValue(false);
        }
    }
} 