package com.calmerdiary.util;

import android.util.Patterns;

/** Validações reutilizáveis dos formulários. */
public final class ValidationUtils {

    public static final int MIN_PASSWORD_LENGTH = 6;

    private ValidationUtils() {
    }

    public static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static boolean isValidEmail(String email) {
        return email != null && Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches();
    }

    public static boolean isValidName(String name) {
        return name != null && name.trim().length() >= 2;
    }

    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= MIN_PASSWORD_LENGTH;
    }

    public static boolean passwordsMatch(String password, String confirm) {
        return password != null && password.equals(confirm);
    }
}
