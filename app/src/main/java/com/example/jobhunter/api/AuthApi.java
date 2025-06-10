package com.example.jobhunter.api;

import android.content.Context;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import org.json.JSONObject;

public class AuthApi {
    // Đăng nhập (POST /api/auth/login)
    public static void login(Context context, JSONObject loginData, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        String url = ApiConfig.AUTH_LOGIN;
        JsonObjectRequest request = new JsonObjectRequest(
            com.android.volley.Request.Method.POST,
            url,
            loginData,
            listener,
            errorListener
        );
        VolleySingleton.getInstance(context).addToRequestQueue(request);
    }

    // Đăng ký (POST /api/auth/register)
    public static void register(Context context, JSONObject registerData, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        String url = ApiConfig.AUTH_REGISTER;
        JsonObjectRequest request = new JsonObjectRequest(
            com.android.volley.Request.Method.POST,
            url,
            registerData,
            listener,
            errorListener
        );
        VolleySingleton.getInstance(context).addToRequestQueue(request);
    }
}