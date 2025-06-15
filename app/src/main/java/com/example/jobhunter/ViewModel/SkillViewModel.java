package com.example.jobhunter.ViewModel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.LiveData;
import com.example.jobhunter.api.SkillApi;
import com.example.jobhunter.model.Skill;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import android.util.Log;
import com.example.jobhunter.util.TokenManager;

import java.util.ArrayList;
import java.util.List;

public class SkillViewModel extends AndroidViewModel {
    private MutableLiveData<List<Skill>> skills = new MutableLiveData<>();
    private MutableLiveData<JSONObject> skill = new MutableLiveData<>();
    private MutableLiveData<String> error = new MutableLiveData<>();

    public SkillViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<List<Skill>> getSkillsLiveData() {
        return skills;
    }

    public LiveData<JSONObject> getSkillLiveData() {
        return skill;
    }

    public LiveData<String> getErrorLiveData() {
        return error;
    }

    public void fetchSkills() {
        String token = TokenManager.getToken(getApplication());
        if (token == null || token.isEmpty()) {
            // Không set lỗi nếu chưa đăng nhập
            return;
        }
        SkillApi.getSkills(getApplication(), token, response -> {
            try {
                // Thêm log để xem response từ server
                Log.d("SKILL_VIEW_MODEL", "Response: " + response.toString());
                Log.d("SKILLS", String.valueOf(response.has("data")));

                if (response.has("data")) {
                    JSONObject dataObject = response.getJSONObject("data");
                    if (dataObject.has("result")) {
                        JSONArray skillsArray = dataObject.getJSONArray("result");
                        if (skillsArray.length() == 0) {
                            Log.w("SKILL_VIEW_MODEL", "Nhận được danh sách kỹ năng rỗng từ API (result is empty).");
                        }
                        ArrayList<Skill> skillList = new ArrayList<>();
                        for (int i = 0; i < skillsArray.length(); i++) {
                            JSONObject skillJson = skillsArray.getJSONObject(i);
                            long id = skillJson.getLong("id");
                            String name = skillJson.getString("name");
                            String createdAt = skillJson.optString("createdAt", "");
                            String updatedAt = skillJson.optString("updatedAt", "");
                            String createdBy = skillJson.optString("createdBy", "");
                            String updatedBy = skillJson.optString("updatedBy", "");
                            skillList.add(new Skill(id, name, createdAt, updatedAt, createdBy, updatedBy));
                        }
                        skills.setValue(skillList);
                    } else {
                        Log.e("SKILL_VIEW_MODEL", "Đối tượng 'data' không chứa key 'result'.");
                        error.setValue("Dữ liệu kỹ năng trả về không có 'result'.");
                    }
                } else {
                    Log.e("SKILL_VIEW_MODEL", "API response không chứa key 'data'.");
                    error.setValue("Dữ liệu kỹ năng trả về không hợp lệ.");
                }
            } catch (JSONException e) {
                Log.e("SKILL_VIEW_MODEL", "Lỗi phân tích JSON: ", e);
                error.setValue("Lỗi phân tích dữ liệu kỹ năng: " + e.getMessage());
            }
        }, this::handleError);
    }

    public void fetchSkill(String skillId, String token) {
        SkillApi.getSkill(getApplication(), skillId, token, response -> skill.setValue(response), this::handleError);
    }

    private void handleError(VolleyError volleyError) {
        error.setValue(volleyError.getMessage());
        // Thêm log để gỡ lỗi Volley
        Log.e("SKILL_VIEW_MODEL", "Volley Error: " + volleyError.toString());
    }
}