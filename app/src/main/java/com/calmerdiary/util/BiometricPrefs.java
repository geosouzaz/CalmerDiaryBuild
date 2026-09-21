package com.calmerdiary.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.calmerdiary.data.entities.User;

/**
 * Guarda, de forma global, se o login por biometria está habilitado e para
 * qual usuário — permitindo oferecer "Entrar com biometria" na tela de login
 * mesmo após o logout (a sessão em si fica no {@link SessionManager}).
 */
public class BiometricPrefs {

    private static final String PREFS = "calmer_biometric";
    private static final String KEY_ENABLED = "enabled";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_NAME = "name";
    private static final String KEY_EMAIL = "email";

    private final SharedPreferences prefs;

    public BiometricPrefs(Context context) {
        this.prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public void enableFor(User user) {
        prefs.edit()
                .putBoolean(KEY_ENABLED, true)
                .putLong(KEY_USER_ID, user.id)
                .putString(KEY_NAME, user.name)
                .putString(KEY_EMAIL, user.email)
                .apply();
    }

    public void disable() {
        prefs.edit().clear().apply();
    }

    public boolean isEnabled() {
        return prefs.getBoolean(KEY_ENABLED, false);
    }

    public long getUserId() {
        return prefs.getLong(KEY_USER_ID, -1L);
    }

    public String getName() {
        return prefs.getString(KEY_NAME, "");
    }

    public String getEmail() {
        return prefs.getString(KEY_EMAIL, "");
    }
}
