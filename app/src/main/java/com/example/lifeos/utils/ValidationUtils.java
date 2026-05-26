package com.example.lifeos.utils;

import android.text.TextUtils;
import android.util.Patterns;

public final class ValidationUtils {

    private ValidationUtils() {}

    public static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }

    public static boolean isValidUsername(String username) {
        return username != null && username.matches("^[a-zA-Z0-9_]{3,20}$");
    }

    public static boolean passwordsMatch(String password, String confirm) {
        return password != null && password.equals(confirm);
    }
}
