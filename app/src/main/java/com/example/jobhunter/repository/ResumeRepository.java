package com.example.jobhunter.repository;

import android.content.Context;
import com.android.volley.Response;
import org.json.JSONObject;
import org.json.JSONArray;
import com.example.jobhunter.api.ResumeApi;

public class ResumeRepository {
    public void getResumes(Context context, String token, Response.Listener<JSONArray> listener, Response.ErrorListener errorListener) {
        ResumeApi.getResumes(context, token, listener, errorListener);
    }

    public void getResume(Context context, String resumeId, String token, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        ResumeApi.getResume(context, resumeId, token, listener, errorListener);
    }

    public void createResume(Context context, JSONObject resumeData, String token, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        ResumeApi.createResume(context, resumeData, token, listener, errorListener);
    }

    public void updateResume(Context context, JSONObject resumeData, String token, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        ResumeApi.updateResume(context, resumeData, token, listener, errorListener);
    }

    public void deleteResume(Context context, String resumeId, String token, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        ResumeApi.deleteResume(context, resumeId, token, listener, errorListener);
    }
}