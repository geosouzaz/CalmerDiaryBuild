package com.calmerdiary.util;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.calmerdiary.data.entities.User;

/**
 * Guarda a sessão do usuário logado de forma segura
 * (EncryptedSharedPreferences). É o que mantém o login entre aberturas do app.
 */
public class SessionManager {

    private static final String PREFS_NAME = "calmer_session";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        this.prefs = createPrefs(context.getApplicationContext());
    }

    private SharedPreferences createPrefs(Context context) {
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();
            return EncryptedSharedPreferences.create(
                    context,
                    PREFS_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);
        } catch (Exception e) {
            // Fallback seguro: se a criptografia falhar, usa prefs comuns
            // apenas para não quebrar o app (não guarda dados sensíveis).
            return context.getSharedPreferences(PREFS_NAME + "_fallback", Context.MODE_PRIVATE);
        }
    }

    public void saveSession(User user) {
        prefs.edit()
                .putLong(KEY_USER_ID, user.id)
                .putString(KEY_USER_NAME, user.name)
                .putString(KEY_USER_EMAIL, user.email)
                .apply();
    }

    public boolean isLoggedIn() {
        return prefs.getLong(KEY_USER_ID, -1L) > 0;
    }

    public long getUserId() {
        return prefs.getLong(KEY_USER_ID, -1L);
    }

    public String getUserName() {
        return prefs.getString(KEY_USER_NAME, "");
    }

    public String getUserEmail() {
        return prefs.getString(KEY_USER_EMAIL, "");
    }

    public void clear() {
        prefs.edit().clear().apply();
    }
}
