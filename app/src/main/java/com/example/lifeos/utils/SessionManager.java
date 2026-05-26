package com.example.lifeos.utils;

import android.content.Context;
import android.content.SharedPreferences;

/** Persists login preference for auto-login. */
public class SessionManager {

    private static final String PREFS = "lifeos_session";
    private static final String KEY_REMEMBER = "remember_login";
    private static final String KEY_USER_ID = "user_id";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public void setRememberLogin(boolean remember) {
        prefs.edit().putBoolean(KEY_REMEMBER, remember).apply();
    }

    public boolean isRememberLogin() {
        return prefs.getBoolean(KEY_REMEMBER, true);
    }

    public void setUserId(String userId) {
        prefs.edit().putString(KEY_USER_ID, userId).apply();
    }

    public String getUserId() {
        return prefs.getString(KEY_USER_ID, null);
    }

    public void clear() {
        prefs.edit().clear().apply();
    }
}
