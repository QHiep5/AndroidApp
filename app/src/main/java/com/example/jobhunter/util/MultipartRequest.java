package com.example.jobhunter.util;
 // chỉnh lại package phù hợp

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class MultipartRequest extends Request<NetworkResponse> {

    private final Response.Listener<NetworkResponse> mListener;
    private final Map<String, String> mHeaders;
    private final Map<String, String> mParams;
    private final Map<String, DataPart> mByteData;
    private final String boundary;
    private static final String LINE_FEED = "\r\n";

    public MultipartRequest(String url,
                            Response.Listener<NetworkResponse> listener,
                            Response.ErrorListener errorListener) {
        super(Method.POST, url, errorListener);
        this.mListener = listener;
        this.mHeaders = new HashMap<>();
        this.mParams = new HashMap<>();
        this.mByteData = new HashMap<>();
        this.boundary = "apiclient-" + System.currentTimeMillis();
    }

    public void addStringParam(String key, String value) {
        mParams.put(key, value);
    }

    public void addFile(String key, byte[] fileData, String filename, String contentType) {
        mByteData.put(key, new DataPart(filename, fileData, contentType));
    }

    @Override
    public String getBodyContentType() {
        return "multipart/form-data;boundary=" + boundary;
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        try {
            // Text params
            for (Map.Entry<String, String> entry : mParams.entrySet()) {
                bos.write(("--" + boundary + LINE_FEED).getBytes());
                bos.write(("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"" + LINE_FEED).getBytes());
                bos.write(("Content-Type: text/plain; charset=UTF-8" + LINE_FEED).getBytes());
                bos.write(LINE_FEED.getBytes());
                bos.write(entry.getValue().getBytes("UTF-8"));
                bos.write(LINE_FEED.getBytes());
            }

            // File params
            for (Map.Entry<String, DataPart> entry : mByteData.entrySet()) {
                DataPart part = entry.getValue();
                bos.write(("--" + boundary + LINE_FEED).getBytes());
                bos.write(("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"; filename=\"" + part.fileName + "\"" + LINE_FEED).getBytes());
                bos.write(("Content-Type: " + part.type + LINE_FEED).getBytes());
                bos.write(LINE_FEED.getBytes());
                bos.write(part.content);
                bos.write(LINE_FEED.getBytes());
            }

            bos.write(("--" + boundary + "--" + LINE_FEED).getBytes());

        } catch (IOException e) {
            e.printStackTrace();
        }

        return bos.toByteArray();
    }

    @Override
    protected Response<NetworkResponse> parseNetworkResponse(NetworkResponse response) {
        return Response.success(response, HttpHeaderParser.parseCacheHeaders(response));
    }

    @Override
    protected void deliverResponse(NetworkResponse response) {
        mListener.onResponse(response);
    }

    public static class DataPart {
        public String fileName;
        public byte[] content;
        public String type;

        public DataPart(String name, byte[] data, String type) {
            this.fileName = name;
            this.content = data;
            this.type = type;
        }
    }
}
