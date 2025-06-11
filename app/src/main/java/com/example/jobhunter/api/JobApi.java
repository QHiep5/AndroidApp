package com.example.jobhunter.api;

import android.content.Context;
import com.android.volley.Response;
import com.android.volley.AuthFailureError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonArrayRequest;
import org.json.JSONObject;
import org.json.JSONArray;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class JobApi {

    private static final String TAG = "JobApi";
    private static final String BASE_URL = ApiConfig.BASE_URL + "jobs";

    // Lấy danh sách job (GET /api/v1/jobs)
    public static void getJobs(Context context, String token, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        Log.d(TAG, "getJobs called.");
        String url = BASE_URL;
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
                    Log.d(TAG, "Authorization Header Added.");
                } else {
                    Log.w(TAG, "No auth token provided. Request sent without Authorization header.");
                }
                return headers;
            }
        };
        Log.i(TAG, "Request URL: " + url);
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
    public static void deleteJob(Context context, String jobId, String token, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        String url = BASE_URL + "/" + jobId;
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

    public static void searchJobs(Context context, String token, String location, List<String> skills, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        Uri.Builder builder = Uri.parse(BASE_URL).buildUpon();
        if (location != null && !location.isEmpty() && !location.equals("Tất cả địa điểm")) {
            builder.appendQueryParameter("location", location);
        }
        if (skills != null && !skills.isEmpty()) {
            builder.appendQueryParameter("skills", TextUtils.join(",", skills));
        }

        String url = builder.build().toString();
        Log.d(TAG, "searchJobs called.");

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(com.android.volley.Request.Method.GET, url, null, listener, errorListener) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                if (token != null && !token.isEmpty()) {
                    headers.put("Authorization", "Bearer " + token);
                    Log.d(TAG, "Authorization Header Added to search request.");
                }
                return headers;
            }
        };
        Log.i(TAG, "Search Request URL: " + url);
        VolleySingleton.getInstance(context).addToRequestQueue(jsonObjectRequest);
    }
}