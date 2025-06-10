package com.example.jobhunter.ViewModel;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import org.json.JSONObject;
import java.io.*;
import okhttp3.*;

public class UploadCVViewModel extends AndroidViewModel {
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> uploadResult = new MutableLiveData<>();
    private final MutableLiveData<String> submitResult = new MutableLiveData<>();

    public UploadCVViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getUploadResult() {
        return uploadResult;
    }

    public LiveData<String> getSubmitResult() {
        return submitResult;
    }

    public void uploadFileToServer(Application app, Uri fileUri, String fileName) {
        isLoading.postValue(true);
        new Thread(() -> {
            try {
                File file = getFileFromUri(app, fileUri, fileName);
                OkHttpClient client = new OkHttpClient();

                RequestBody fileBody = RequestBody.create(MediaType.parse("application/octet-stream"), file);
                MultipartBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("file", fileName, fileBody)
                        .addFormDataPart("folder", "resume")
                        .build();

                SharedPreferences prefs = app.getSharedPreferences("jobhunter_prefs", Context.MODE_PRIVATE);
                String token = prefs.getString("auth_token", null);

                android.util.Log.d("UPLOAD_CV", "Token: " + token);
                android.util.Log.d("UPLOAD_CV", "File name: " + fileName + ", size: " + file.length());

                Request.Builder requestBuilder = new Request.Builder()
                        .url("http://192.168.0.114:8080/api/v1/files")
                        .post(requestBody);
                if (token != null && !token.isEmpty()) {
                    requestBuilder.addHeader("Authorization", "Bearer " + token);
                }
                Request request = requestBuilder.build();

                android.util.Log.d("UPLOAD_CV", "Headers: " + request.headers().toString());

                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    String resStr = response.body().string();
                    android.util.Log.d("UPLOAD_CV", "Response: " + resStr);
                    JSONObject resJson = new JSONObject(resStr);
                    JSONObject dataObj = resJson.optJSONObject("data");
                    String uploadedFileName = dataObj != null ? dataObj.getString("fileName") : null;
                    uploadResult.postValue(uploadedFileName);
                } else {
                    String errorBody = response.body() != null ? response.body().string() : "null";
                    int code = response.code();
                    String msg = response.message();
                    android.util.Log.e("UPLOAD_CV",
                            "Upload failed! Code: " + code + ", Message: " + msg + ", Body: " + errorBody);
                    uploadResult.postValue(null);
                }
            } catch (Exception e) {
                android.util.Log.e("UPLOAD_CV", "Exception: " + e.getMessage(), e);
                uploadResult.postValue(null);
            } finally {
                isLoading.postValue(false);
            }
        }).start();
    }

    public void submitResume(String email, String fileName, long userId, long jobId) {
        isLoading.postValue(true);
        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();
                JSONObject body = new JSONObject();
                body.put("email", email);
                body.put("url", fileName);
                body.put("status", "PENDING");

                JSONObject userObj = new JSONObject();
                userObj.put("id", userId);
                body.put("user", userObj);

                JSONObject jobObj = new JSONObject();
                jobObj.put("id", jobId);
                body.put("job", jobObj);

                RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), body.toString());

                SharedPreferences prefs = getApplication().getSharedPreferences("jobhunter_prefs",
                        Context.MODE_PRIVATE);
                String token = prefs.getString("auth_token", null);

                Request.Builder requestBuilder = new Request.Builder()
                        .url("http://192.168.0.114:8080/api/v1/resumes")
                        .post(requestBody);
                if (token != null && !token.isEmpty()) {
                    requestBuilder.addHeader("Authorization", "Bearer " + token);
                }
                Request request = requestBuilder.build();

                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    submitResult.postValue("success");
                } else {
                    submitResult.postValue("fail");
                }
            } catch (Exception e) {
                submitResult.postValue("fail");
            } finally {
                isLoading.postValue(false);
            }
        }).start();
    }

    private File getFileFromUri(Application app, Uri uri, String fileName) throws IOException {
        InputStream inputStream = app.getContentResolver().openInputStream(uri);
        File tempFile = new File(app.getCacheDir(), fileName);
        OutputStream outputStream = new FileOutputStream(tempFile);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, length);
        }
        outputStream.close();
        inputStream.close();
        return tempFile;
    }
}