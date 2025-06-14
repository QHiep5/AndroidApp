package com.example.jobhunter.api;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class MultipartRequest extends Request<String> {
    private static final String TAG = "MultipartRequest";
    private final Response.Listener<String> mListener;
    private final Map<String, String> mParams;
    private final Map<String, DataPart> mByteData;
    private String token;

    public MultipartRequest(String url, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(Method.POST, url, errorListener);
        mListener = listener;
        mParams = new HashMap<>();
        mByteData = new HashMap<>();
    }

    public void setHeaders(String token) {
        this.token = token;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> headers = super.getHeaders();
        if (token != null && !token.isEmpty()) {
            headers.put("Authorization", "Bearer " + token);
        }
        return headers;
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return mParams;
    }

    protected Map<String, DataPart> getByteData() {
        return mByteData;
    }

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        try {
            String jsonString = new String(response.data, StandardCharsets.UTF_8);
            return Response.success(jsonString, getCacheEntry());
        } catch (Exception e) {
            return Response.error(new VolleyError("Error parsing response"));
        }
    }

    @Override
    protected void deliverResponse(String response) {
        mListener.onResponse(response);
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

            // Add string parameters
            if (mParams != null) {
                for (Map.Entry<String, String> entry : mParams.entrySet()) {
                    builder.addPart(entry.getKey(), 
                        new StringBody(entry.getValue(), ContentType.TEXT_PLAIN));
                }
            }

            // Add file data
            if (mByteData != null) {
                for (Map.Entry<String, DataPart> entry : mByteData.entrySet()) {
                    DataPart dataPart = entry.getValue();
                    File file = new File(dataPart.fileName);
                    builder.addPart(entry.getKey(), 
                        new FileBody(file, ContentType.APPLICATION_OCTET_STREAM, file.getName()));
                }
            }

            HttpEntity entity = builder.build();
            entity.writeTo(bos);
        } catch (IOException e) {
            Log.e(TAG, "IOException writing to ByteArrayOutputStream", e);
        }
        return bos.toByteArray();
    }

    @Override
    public String getBodyContentType() {
        return "multipart/form-data";
    }

    public static class DataPart {
        public String fileName;
        public byte[] content;

        public DataPart(String fileName, byte[] content) {
            this.fileName = fileName;
            this.content = content;
        }
    }
} 