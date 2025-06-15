package com.example.jobhunter.api;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.jobhunter.utils.FilterQueryBuilder;

import org.json.JSONObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JobApi {

    private static final String TAG = "JobApi";
    private static final String BASE_URL = ApiConfig.BASE_URL + "jobs";

    // Lấy danh sách job (GET /api/v1/jobs)
    public static void getJobs(Context context, String token, int page, int pageSize, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        String url = ApiConfig.JOB + "?page=" + page + "&pageSize=" + pageSize;
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
        String url = BASE_URL + "/" + jobId;
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

    // Tạo job (POST /api/v1/jobs)
    public static void createJob(Context context, JSONObject jobData, String token, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        String url = BASE_URL;
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
        String url = BASE_URL;
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
    public static void deleteJob(Context context, long jobId, String token,
                                com.android.volley.Response.Listener<String> listener,
                                com.android.volley.Response.ErrorListener errorListener) {
        String url = ApiConfig.BASE_URL + "jobs/" + jobId;
        com.android.volley.toolbox.StringRequest request = new com.android.volley.toolbox.StringRequest(
                com.android.volley.Request.Method.DELETE, url, listener, errorListener) {
            @Override
            public java.util.Map<String, String> getHeaders() {
                java.util.Map<String, String> headers = new java.util.HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };
        com.android.volley.toolbox.Volley.newRequestQueue(context).add(request);
    }

    public static void searchJobs(Context context, String token, String location, List<String> skillIds, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        Map<String, List<String>> filters = new HashMap<>();

        if (location != null && !location.isEmpty() && !location.equals("Tất cả địa điểm")) {
            filters.put("location", Collections.singletonList(location));
        }

        if (skillIds != null && !skillIds.isEmpty()) {
            filters.put("skills", skillIds);
        }

        String encodedFilter = FilterQueryBuilder.buildFilterQuery(filters);

        String url = BASE_URL;
        if (!encodedFilter.isEmpty()) {
            url += "?filter=" + encodedFilter;
        }

        Log.i("API_URL_DEBUG", "URL gửi đi: " + url);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(com.android.volley.Request.Method.GET, url, null, listener, errorListener) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                if (token != null && !token.isEmpty()) {
                    headers.put("Authorization", "Bearer " + token);
                }
                return headers;
            }
        };
        VolleySingleton.getInstance(context).addToRequestQueue(jsonObjectRequest);
    }
}