package com.example.jobhunter.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME = "JobHunterPref";
    private static final String KEY_AUTH_TOKEN = "auth_token";

    private final SharedPreferences pref;
    private final SharedPreferences.Editor editor;
    private final Context _context;

    // Constructor
    public SessionManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    /**
     * Saves the authentication token to SharedPreferences.
     * @param token The token string to save.
     */
    public void saveAuthToken(String token) {
        editor.putString(KEY_AUTH_TOKEN, token);
        editor.apply(); // Use apply() for asynchronous saving
    }

    /**
     * Retrieves the authentication token from SharedPreferences.
     * @return The saved token, or null if no token is found.
     */
    public String getAuthToken() {
        return pref.getString(KEY_AUTH_TOKEN, null);
    }

    /**
     * Checks if the user is logged in.
     * @return true if an auth token exists, false otherwise.
     */
    public boolean isLoggedIn() {
        return getAuthToken() != null && !getAuthToken().isEmpty();
    }

    /**
     * Clears the session data (e.g., on logout).
     */
    public void clearSession() {
        editor.clear();
        editor.apply();
    }
} 