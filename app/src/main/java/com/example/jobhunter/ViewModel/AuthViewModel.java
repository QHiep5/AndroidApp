package com.example.jobhunter.ViewModel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.LiveData;
import com.example.jobhunter.api.AuthApi;
import org.json.JSONObject;
import com.android.volley.Response;
import com.android.volley.VolleyError;

public class  AuthViewModel extends AndroidViewModel {
    private MutableLiveData<JSONObject> loginResult = new MutableLiveData<>();
    private MutableLiveData<JSONObject> registerResult = new MutableLiveData<>();
    private MutableLiveData<String> error = new MutableLiveData<>();

    public AuthViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<JSONObject> getLoginResult() { return loginResult; }
    public LiveData<JSONObject> getRegisterResult() { return registerResult; }
    public LiveData<String> getErrorLiveData() { return error; }

    public void login(JSONObject loginData) {
        AuthApi.login(getApplication(), loginData, response -> loginResult.setValue(response), this::handleError);
    }

    public void register(JSONObject registerData) {
        AuthApi.register(getApplication(), registerData, response -> registerResult.setValue(response), this::handleError);
    }

    private void handleError(VolleyError volleyError) {
        if (volleyError.networkResponse != null && volleyError.networkResponse.data != null) {
            error.setValue(new String(volleyError.networkResponse.data));
        } else {
            error.setValue(volleyError.getMessage());
        }
    }
}