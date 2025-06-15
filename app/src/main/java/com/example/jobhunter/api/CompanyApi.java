package com.example.jobhunter.api;
import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.database.Cursor;
import android.provider.OpenableColumns;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.AuthFailureError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ByteArrayOutputStream;

public class CompanyApi {
    private static final String TAG = "CompanyApi";
    private static final String BASE_URL = ApiConfig.BASE_URL; // Replace with your actual API base URL

    // Lấy danh sách company (GET /api/v1/companies)
    public static void getCompanies(Context context, String token, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        String url = ApiConfig.COMPANY;
        JsonObjectRequest request = new JsonObjectRequest(
            com.android.volley.Request.Method.GET,
            url,
            null,
            listener,
            errorListener
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                if (token != null && !token.isEmpty()) {
                    headers.put("Authorization", "Bearer " + token);
                }
                return headers;
            }
        };
        VolleySingleton.getInstance(context).addToRequestQueue(request);
    }

    // Lấy company theo id (GET /api/v1/companies/{id})
    public static void getCompany(Context context, String companyId, String token, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        String url = ApiConfig.COMPANY + "/" + companyId;
        JsonObjectRequest request = new JsonObjectRequest(
            com.android.volley.Request.Method.GET,
            url,
            null,
            listener,
            errorListener
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                if (token != null && !token.isEmpty()) {
                    headers.put("Authorization", "Bearer " + token);
                }
                return headers;
            }
        };
        VolleySingleton.getInstance(context).addToRequestQueue(request);
    }

    // Tạo company (POST /api/v1/companies)
    public static void createCompany(Context context, JSONObject companyData, String token, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        String url = ApiConfig.COMPANY;
        JsonObjectRequest request = new JsonObjectRequest(
            com.android.volley.Request.Method.POST,
            url,
            companyData,
            listener,
            errorListener
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };
        VolleySingleton.getInstance(context).addToRequestQueue(request);
    }

    // Cập nhật company (PUT /api/v1/companies)
    public static void updateCompany(Context context, JSONObject companyData, String token, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        String url = ApiConfig.COMPANY;
        JsonObjectRequest request = new JsonObjectRequest(
            com.android.volley.Request.Method.PUT,
            url,
            companyData,
            listener,
            errorListener
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
//                headers.put("Authorization", "Bearer " + token);  // <-- token ở đây
                headers.put("Content-Type", "application/json");
                return headers;
            }

        };
        VolleySingleton.getInstance(context).addToRequestQueue(request);
    }

    // Xóa company (DELETE /api/v1/companies/{id})
    public static void deleteCompany(Context context, String companyId, String token,
                                     Response.Listener<JSONObject> listener,
                                     Response.ErrorListener errorListener) {
        String url = ApiConfig.COMPANY + "/" + companyId;

        // Log URL
        Log.d("DeleteCompany", "DELETE URL: " + url);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.DELETE,
                url,
                null,
                response -> {
                    Log.d("DeleteCompany", "Response: " + response.toString());
                    listener.onResponse(response);
                },
                error -> {
                    Log.e("DeleteCompany", "Error: " + error.toString());
                    errorListener.onErrorResponse(error);
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                if (token != null && !token.isEmpty()) {
//                    headers.put("Authorization", "Bearer " + token);
                    Log.d("DeleteCompany", "Authorization: Bearer " + token);
                } else {
                    Log.w("DeleteCompany", "No token provided");
                }
                headers.put("Content-Type", "application/json");
                Log.d("DeleteCompany", "Headers: " + headers.toString());
                return headers;
            }
        };
        VolleySingleton.getInstance(context).addToRequestQueue(request);
    }


    public static void uploadFile(Context context, Uri fileUri, String companyName, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        try {
            String url = ApiConfig.FILE;

            // Get the original file extension
            String extension = "";
            String mimeType = context.getContentResolver().getType(fileUri);
            if (mimeType != null) {
                extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
                if (extension != null) {
                    extension = "." + extension;
                } else {
                    extension = "";
                }
            }

            // Normalize companyName: remove Vietnamese diacritics and join with hyphens
            String normalizedName = Normalizer.normalize(companyName, Normalizer.Form.NFD)
                    .replaceAll("\\p{M}", "") // Remove diacritics
                    .replaceAll("[^\\p{Alnum}]", "-") // Replace non-alphanumeric with hyphen
                    .replaceAll("-+", "-") // Replace multiple hyphens with single hyphen
                    .toLowerCase(Locale.getDefault())
                    .replaceAll("^-|-$", ""); // Remove leading/trailing hyphens

            // Convert Uri to File using normalized companyName with original extension
            File file = new File(context.getCacheDir(), normalizedName + extension);
            try (InputStream inputStream = context.getContentResolver().openInputStream(fileUri);
                 FileOutputStream outputStream = new FileOutputStream(file)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            Map<String, String> params = new HashMap<>();
            params.put("folder", "company");

            Map<String, VolleyMultipartRequest.DataPart> byteData = new HashMap<>();
            byteData.put("file", new VolleyMultipartRequest.DataPart(file.getName(), file));

            Log.d("Filess", String.valueOf(file.length()));
            // Create multipart request
            VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(
                    Request.Method.POST,
                    url,
                    params,
                    byteData,
                    response -> {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            listener.onResponse(jsonResponse);
                        } catch (JSONException e) {
                            errorListener.onErrorResponse(new VolleyError("Error parsing response"));
                        }
                    },
                    errorListener
            ) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("folder", "company");
                    return params;
                }

                @Override
                protected Map<String, DataPart> getByteData() {
                    Map<String, DataPart> params = new HashMap<>();
                    try {
                        params.put("file", new DataPart(file.getName(), file));
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading file", e);
                        errorListener.onErrorResponse(new VolleyError("Error reading file"));
                    }
                    return params;
                }
            };

            // Add to request queue
            RequestQueue requestQueue = Volley.newRequestQueue(context);
            requestQueue.add(multipartRequest);
        } catch (Exception e) {
            Log.e(TAG, "Error creating request", e);
            errorListener.onErrorResponse(new VolleyError("Error creating request"));
        }
    }

    public static void createCompany(Context context, String name, String address, String description, String logo, 
                                   Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        try {
            String url = ApiConfig.COMPANY;
            
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("name", name);
            jsonBody.put("address", address);
            jsonBody.put("description", description);
            jsonBody.put("logo", logo);
            jsonBody.put("createdAt", JSONObject.NULL);
            jsonBody.put("updatedAt", JSONObject.NULL);
            jsonBody.put("createdBy", JSONObject.NULL);
            jsonBody.put("updatedBy", JSONObject.NULL);

            JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                jsonBody,
                listener,
                errorListener
            ) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    return headers;
                }
            };

            RequestQueue requestQueue = Volley.newRequestQueue(context);
            requestQueue.add(request);
        } catch (JSONException e) {
            errorListener.onErrorResponse(new VolleyError("Error creating request"));
        }
    }

    @SuppressLint("Range")
    private static String getFileName(Context context, Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf(File.separator);
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }
}