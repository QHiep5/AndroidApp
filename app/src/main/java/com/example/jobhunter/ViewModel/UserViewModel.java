package com.example.jobhunter.ViewModel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.android.volley.VolleyError;
import com.example.jobhunter.api.UserApi;
import com.example.jobhunter.model.Skill;
import com.example.jobhunter.model.User;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class UserViewModel extends AndroidViewModel {
    private final MutableLiveData<JSONArray> users = new MutableLiveData<>();
    private final MutableLiveData<User> user = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public UserViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<JSONArray> getUsersLiveData() { return users; }
    public LiveData<User> getUserLiveData() { return user; }
    public LiveData<String> getErrorLiveData() { return error; }

    public void fetchUsers(String token) {
        UserApi.getUsers(getApplication(), token, users::setValue, this::handleError);
    }

    public void fetchUser(String userId, String token) {
        UserApi.getUser(getApplication(), userId, token, response -> {
            try {
                JSONObject userJson = response.getJSONObject("data");
                User parsedUser = parseUserFromJson(userJson);
                user.setValue(parsedUser);
            } catch (JSONException e) {
                Log.e("UserViewModel", "Error parsing user data", e);
                error.setValue("Lỗi phân tích dữ liệu người dùng: " + e.getMessage());
            }
        }, this::handleError);
    }

    private User parseUserFromJson(JSONObject userJson) throws JSONException {
        User userObj = new User();
        userObj.setId(userJson.optInt("id"));
        userObj.setName(userJson.optString("name"));
        userObj.setEmail(userJson.optString("email"));
        
        // Assuming other fields like address, phone, gender, age are in User model
        if (userJson.has("company")) {
            // Assuming there's a company object/ID to parse
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
            Log.d("UserViewModel", "Parsed " + skillsList.size() + " skills for user: " + userObj.getName());
        }
        return userObj;
    }

    private void handleError(VolleyError volleyError) {
        Log.e("UserViewModel", "API Error", volleyError);
        error.setValue("Lỗi mạng: " + volleyError.getMessage());
    }
}