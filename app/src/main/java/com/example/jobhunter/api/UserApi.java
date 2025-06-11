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

import android.content.Context;
import com.android.volley.Response;
import com.android.volley.AuthFailureError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonArrayRequest;
import org.json.JSONObject;
import org.json.JSONArray;
import java.util.HashMap;
import java.util.Map;

public class UserApi {
    // Lấy danh sách user (GET /api/v1/users)
    public static void getUsers(Context context, String token, Response.Listener<JSONArray> listener, Response.ErrorListener errorListener) {
        String url = ApiConfig.USER;
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
                if (token != null && !token.isEmpty()) {
                    headers.put("Authorization", "Bearer " + token);
                }
                return headers;
            }
        };
        VolleySingleton.getInstance(context).addToRequestQueue(request);
    }

    // Lấy user theo id (GET /api/v1/users/{id})
    public static void getUser(Context context, String userId, String token, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        String url = ApiConfig.USER +"/"+ userId;
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

    // Tạo user (POST /api/v1/users)
    public static void createUser(Context context, JSONObject userData, String token, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        String url = ApiConfig.USER;
        JsonObjectRequest request = new JsonObjectRequest(
                com.android.volley.Request.Method.POST,
                url,
                userData,
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

    // Cập nhật user (PUT /api/v1/users)
    public static void updateUser(Context context, JSONObject userData, String token, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        String url = ApiConfig.USER;
        JsonObjectRequest request = new JsonObjectRequest(
                com.android.volley.Request.Method.PUT,
                url,
                userData,
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

    // Xóa user (DELETE /api/v1/users/{id})
    public static void deleteUser(Context context, String userId, String token, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        String url = ApiConfig.USER + userId;
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
                if (token != null && !token.isEmpty()) {
                    headers.put("Authorization", "Bearer " + token);
                }
                return headers;
            }
        };
        VolleySingleton.getInstance(context).addToRequestQueue(request);
    }
}
