package com.example.jobhunter.ViewModel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.LiveData;
import com.example.jobhunter.api.UserApi;
import org.json.JSONArray;
import org.json.JSONObject;
import com.android.volley.Response;
import com.android.volley.VolleyError;

public class UserViewModel extends AndroidViewModel {
    private MutableLiveData<JSONArray> users = new MutableLiveData<>();
    private MutableLiveData<JSONObject> user = new MutableLiveData<>();
    private MutableLiveData<String> error = new MutableLiveData<>();

    public UserViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<JSONArray> getUsersLiveData() { return users; }
    public LiveData<JSONObject> getUserLiveData() { return user; }
    public LiveData<String> getErrorLiveData() { return error; }

    public void fetchUsers(String token) {
        UserApi.getUsers(getApplication(), token, response -> users.setValue(response), this::handleError);
    }

    public void fetchUser(String userId, String token) {
        UserApi.getUser(getApplication(), userId, token, response -> user.setValue(response), this::handleError);
    }

    private void handleError(VolleyError volleyError) {
        error.setValue(volleyError.getMessage());
    }
}