package com.example.jobhunter.repository;

import android.content.Context;
import com.android.volley.Response;
import org.json.JSONObject;
import org.json.JSONArray;
import com.example.jobhunter.api.CompanyApi;

public class CompanyRepository {

    public void getCompany(Context context, String companyId, String token, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        CompanyApi.getCompany(context, companyId, token, listener, errorListener);
    }

    public void createCompany(Context context, JSONObject companyData, String token, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        CompanyApi.createCompany(context, companyData, token, listener, errorListener);
    }

    public void updateCompany(Context context, JSONObject companyData, String token, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        CompanyApi.updateCompany(context, companyData, token, listener, errorListener);
    }

    public void deleteCompany(Context context, String companyId, String token, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        CompanyApi.deleteCompany(context, companyId, token, listener, errorListener);
    }
}