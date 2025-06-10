package com.example.jobhunter.repository;

import android.content.Context;
import com.android.volley.Response;
import org.json.JSONObject;
import org.json.JSONArray;
import com.example.jobhunter.api.JobApi;

public class JobRepository {
    public void getJobs(Context context, String token, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        JobApi.getJobs(context, token, listener, errorListener);
    }

    public void getJob(Context context, String jobId, String token, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        JobApi.getJob(context, jobId, token, listener, errorListener);
    }

    public void createJob(Context context, JSONObject jobData, String token, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        JobApi.createJob(context, jobData, token, listener, errorListener);
    }

    public void updateJob(Context context, JSONObject jobData, String token, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        JobApi.updateJob(context, jobData, token, listener, errorListener);
    }

    public void deleteJob(Context context, String jobId, String token, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        JobApi.deleteJob(context, jobId, token, listener, errorListener);
    }
}