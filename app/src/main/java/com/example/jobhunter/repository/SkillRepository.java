package com.example.jobhunter.repository;


import android.content.Context;
import com.android.volley.Response;
import org.json.JSONObject;
import org.json.JSONArray;
import com.example.jobhunter.api.SkillApi;

public class SkillRepository {
    public void getSkills(Context context, String token, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        SkillApi.getSkills(context, token, listener, errorListener);
    }

    public void getSkill(Context context, String skillId, String token, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        SkillApi.getSkill(context, skillId, token, listener, errorListener);
    }

    public void createSkill(Context context, JSONObject skillData, String token, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        SkillApi.createSkill(context, skillData, token, listener, errorListener);
    }

    public void updateSkill(Context context, JSONObject skillData, String token, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        SkillApi.updateSkill(context, skillData, token, listener, errorListener);
    }

    public void deleteSkill(Context context, String skillId, String token, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        SkillApi.deleteSkill(context, skillId, token, listener, errorListener);
    }
}