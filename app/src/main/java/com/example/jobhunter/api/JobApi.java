package com.example.jobhunter.api;

import android.content.Context;
import com.android.volley.Response;
import com.android.volley.AuthFailureError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonArrayRequest;
import org.json.JSONObject;
import org.json.JSONArray;
import java.util.HashMap;
import java.util.Map;

public class JobApi {
    // Lấy danh sách job (GET /api/v1/jobs)
    public static void getJobs(Context context, String token, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        String url = ApiConfig.JOB;
        JsonObjectRequest request = new JsonObjectRequest(
            com.android.volley.Request.Method.GET,
            url,
            null,
            listener,
            errorListener
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                if (token != null && !token.isEmpty()) {
                    headers.put("Authorization", "Bearer " + token);
                }
                return headers;
            }
        };
        VolleySingleton.getInstance(context).addToRequestQueue(request);
    }

    // Lấy job theo id (GET /api/v1/jobs/{id})
    public static void getJob(Context context, String jobId, String token, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        String url = ApiConfig.JOB + jobId;
        JsonObjectRequest request = new JsonObjectRequest(
            com.android.volley.Request.Method.GET,
            url,
            null,
            listener,
            errorListener
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };
        VolleySingleton.getInstance(context).addToRequestQueue(request);
    }

    // Tạo job (POST /api/v1/jobs)
    public static void createJob(Context context, JSONObject jobData, String token, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        String url = ApiConfig.JOB;
        JsonObjectRequest request = new JsonObjectRequest(
            com.android.volley.Request.Method.POST,
            url,
            jobData,
            listener,
            errorListener
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };
        VolleySingleton.getInstance(context).addToRequestQueue(request);
    }

    // Cập nhật job (PUT /api/v1/jobs)
    public static void updateJob(Context context, JSONObject jobData, String token, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        String url = ApiConfig.JOB;
        JsonObjectRequest request = new JsonObjectRequest(
            com.android.volley.Request.Method.PUT,
            url,
            jobData,
            listener,
            errorListener
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };
        VolleySingleton.getInstance(context).addToRequestQueue(request);
    }

    // Xóa job (DELETE /api/v1/jobs/{id})
    public static void deleteJob(Context context, String jobId, String token, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        String url = ApiConfig.JOB + jobId;
        JsonObjectRequest request = new JsonObjectRequest(
            com.android.volley.Request.Method.DELETE,
            url,
            null,
            listener,
            errorListener
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };
        VolleySingleton.getInstance(context).addToRequestQueue(request);
    }
}