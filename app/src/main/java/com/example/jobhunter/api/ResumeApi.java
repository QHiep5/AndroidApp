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
import com.android.volley.Request;

public class ResumeApi {
    // Lấy danh sách resume (GET /api/v1/resumes)
    public static void getResumes(Context context, String token, Response.Listener<JSONArray> listener, Response.ErrorListener errorListener) {
        String url = ApiConfig.RESUME;
        JsonArrayRequest request = new JsonArrayRequest(
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

    // Lấy resume theo id (GET /api/v1/resumes/{id})
    public static void getResume(Context context, String resumeId, String token, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        String url = ApiConfig.RESUME + resumeId;
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

    // Tạo resume (POST /api/v1/resumes)
    public static void createResume(Context context, JSONObject resumeData, String token, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        String url = ApiConfig.RESUME;
        JsonObjectRequest request = new JsonObjectRequest(
            com.android.volley.Request.Method.POST,
            url,
            resumeData,
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

    // Cập nhật resume (PUT /api/v1/resumes)
    public static void updateResume(Context context, JSONObject resumeData, String token, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        String url = ApiConfig.RESUME;
        JsonObjectRequest request = new JsonObjectRequest(
            com.android.volley.Request.Method.PUT,
            url,
            resumeData,
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

    // Xóa resume (DELETE /api/v1/resumes/{id})
    public static void deleteResume(Context context, String resumeId, String token, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        String url = ApiConfig.RESUME + resumeId;
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

    public static void getResumesByUser(Context context, String token, int page, int pageSize, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        String url = ApiConfig.RESUME + "/by-user?page=" + page + "&pageSize=" + pageSize;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
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

    // Lấy tất cả resume (GET /api/v1/resumes)
    public static void fetchAllResumes(Context context, String token, int page, int pageSize, 
            com.android.volley.Response.Listener<org.json.JSONObject> listener, 
            com.android.volley.Response.ErrorListener errorListener) {
        String url = ApiConfig.RESUME + "?page=" + page + "&pageSize=" + pageSize;
        com.android.volley.toolbox.JsonObjectRequest request = new com.android.volley.toolbox.JsonObjectRequest(
                com.android.volley.Request.Method.GET, url, null, listener, errorListener) {
            @Override
            public java.util.Map<String, String> getHeaders() throws AuthFailureError {
                java.util.Map<String, String> headers = new java.util.HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };
        VolleySingleton.getInstance(context).addToRequestQueue(request);
    }

    // Cập nhật trạng thái resume (PUT /api/v1/resumes)
    public static void updateResumeState(Context context, long resumeId, com.example.jobhunter.util.constant.ResumeStateEnum newState, String token,
                                         com.android.volley.Response.Listener<org.json.JSONObject> listener,
                                         com.android.volley.Response.ErrorListener errorListener) {
        String url = ApiConfig.RESUME;
        org.json.JSONObject body = new org.json.JSONObject();
        try {
            body.put("id", resumeId);
            body.put("status", newState.name());
        } catch (Exception ignored) {}
        com.android.volley.toolbox.JsonObjectRequest request = new com.android.volley.toolbox.JsonObjectRequest(
                com.android.volley.Request.Method.PUT, url, body, listener, errorListener) {
            @Override
            public java.util.Map<String, String> getHeaders() throws AuthFailureError {
                java.util.Map<String, String> headers = new java.util.HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };
        VolleySingleton.getInstance(context).addToRequestQueue(request);
    }
}
