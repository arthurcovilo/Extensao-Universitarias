package com.example.extensao;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME = "auth_session";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_ROLE = "user_role";

    private final SharedPreferences preferences;

    public SessionManager(Context context) {
        this.preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveSession(String accessToken, String userEmail, String userName, String userRole) {
        preferences.edit()
                .putString(KEY_ACCESS_TOKEN, accessToken)
                .putString(KEY_USER_EMAIL, userEmail)
                .putString(KEY_USER_NAME, userName)
                .putString(KEY_USER_ROLE, userRole)
                .apply();
    }

    public boolean isLoggedIn() {
        String token = preferences.getString(KEY_ACCESS_TOKEN, null);
        return token != null && !token.trim().isEmpty();
    }

    public String getAccessToken() {
        return preferences.getString(KEY_ACCESS_TOKEN, null);
    }

    public String getUserEmail() {
        return preferences.getString(KEY_USER_EMAIL, "");
    }

    public String getUserName() {
        return preferences.getString(KEY_USER_NAME, "");
    }

    public String getUserRole() {
        return preferences.getString(KEY_USER_ROLE, "USER");
    }

    public boolean isAdmin() {
        return "ADMIN".equals(getUserRole());
    }

    public void clearSession() {
        preferences.edit().clear().apply();
    }
}
