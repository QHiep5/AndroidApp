package com.example.jobhunter.repository;

import android.content.Context;
import com.android.volley.Response;
import org.json.JSONObject;
import org.json.JSONArray;
import com.example.jobhunter.api.UserApi;

public class UserRepository {
    public void getUsers(Context context, String token, Response.Listener<JSONArray> listener, Response.ErrorListener errorListener) {
        UserApi.getUsers(context, token, listener, errorListener);
    }

    public void getUser(Context context, String userId, String token, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        UserApi.getUser(context, userId, token, listener, errorListener);
    }

    public void createUser(Context context, JSONObject userData, String token, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        UserApi.createUser(context, userData, token, listener, errorListener);
    }

    public void updateUser(Context context, JSONObject userData, String token, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        UserApi.updateUser(context, userData, token, listener, errorListener);
    }

    public void deleteUser(Context context, String userId, String token, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        UserApi.deleteUser(context, userId, token, listener, errorListener);
    }
}