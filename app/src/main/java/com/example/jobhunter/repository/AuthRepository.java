package com.example.jobhunter.repository;

import android.content.Context;
import com.android.volley.Response;
import org.json.JSONObject;
import com.example.jobhunter.api.AuthApi;

public class AuthRepository {
    public void login(Context context, JSONObject loginData, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        AuthApi.login(context, loginData, listener, errorListener);
    }

    public void register(Context context, JSONObject registerData, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        AuthApi.register(context, registerData, listener, errorListener);
    }
}