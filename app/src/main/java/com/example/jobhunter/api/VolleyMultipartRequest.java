package com.example.jobhunter.api;

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
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class VolleyMultipartRequest extends Request<String> {
    private static final String TAG = "VolleyMultipartRequest";
    private final Response.Listener<String> mListener;
    private final Map<String, String> mParams;
    private final Map<String, DataPart> mByteData;
    private HttpEntity httpEntity;

    public VolleyMultipartRequest(
            int method,
            String url,
            Map<String, String> params,
            Map<String, DataPart> byteData,
            Response.Listener<String> listener,
            Response.ErrorListener errorListener
    ) {
        super(method, url, errorListener);
        this.mListener = listener;
        this.mParams = params;
        this.mByteData = byteData;
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

            if (mParams != null) {
                for (Map.Entry<String, String> entry : mParams.entrySet()) {
                    builder.addPart(entry.getKey(),
                            new StringBody(entry.getValue(), ContentType.TEXT_PLAIN));
                }
            }

            if (mByteData != null) {
                for (Map.Entry<String, DataPart> entry : mByteData.entrySet()) {
                    DataPart dataPart = entry.getValue();
                    builder.addPart(entry.getKey(),
                            new FileBody(dataPart.file, ContentType.APPLICATION_OCTET_STREAM, dataPart.file.getName()));
                }
            }

            httpEntity = builder.build();
            httpEntity.writeTo(bos);
        } catch (IOException e) {
            Log.e(TAG, "IOException writing to ByteArrayOutputStream", e);
        }
        return bos.toByteArray();
    }

    @Override
    public String getBodyContentType() {
        return httpEntity != null ? httpEntity.getContentType().getValue() : "multipart/form-data";
    }

    public static class DataPart {
        public File file;

        public DataPart(String fileName, File file) {
            this.file = file;
        }
    }
}
