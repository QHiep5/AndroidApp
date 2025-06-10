package com.example.jobhunter.api;
import android.content.Context;
import com.android.volley.Response;
import com.android.volley.AuthFailureError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonArrayRequest;
import org.json.JSONObject;
import org.json.JSONArray;
import java.util.HashMap;
import java.util.Map;

public class SkillApi {
    // Lấy danh sách skill (GET /api/v1/skills)
    public static void getSkills(Context context, String token, Response.Listener<JSONArray> listener, Response.ErrorListener errorListener) {
        String url = ApiConfig.SKILL;
        JsonArrayRequest request = new JsonArrayRequest(
            com.android.volley.Request.Method.GET,
            url,
            null,
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

    // Lấy skill theo id (GET /api/v1/skills/{id})
    public static void getSkill(Context context, String skillId, String token, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        String url = ApiConfig.SKILL + skillId;
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
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };
        VolleySingleton.getInstance(context).addToRequestQueue(request);
    }

    // Tạo skill (POST /api/v1/skills)
    public static void createSkill(Context context, JSONObject skillData, String token, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        String url = ApiConfig.SKILL;
        JsonObjectRequest request = new JsonObjectRequest(
            com.android.volley.Request.Method.POST,
            url,
            skillData,
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

    // Cập nhật skill (PUT /api/v1/skills)
    public static void updateSkill(Context context, JSONObject skillData, String token, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        String url = ApiConfig.SKILL;
        JsonObjectRequest request = new JsonObjectRequest(
            com.android.volley.Request.Method.PUT,
            url,
            skillData,
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

    // Xóa skill (DELETE /api/v1/skills/{id})
    public static void deleteSkill(Context context, String skillId, String token, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        String url = ApiConfig.SKILL + skillId;
        JsonObjectRequest request = new JsonObjectRequest(
            com.android.volley.Request.Method.DELETE,
            url,
            null,
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
}