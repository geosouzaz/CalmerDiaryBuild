package com.calmerdiary.util;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

/**
 * Gerencia o tema do app (Sistema / Claro / Escuro).
 * A escolha é persistida e aplicada globalmente com AppCompatDelegate —
 * trocar o modo recria as Activities automaticamente.
 */
public final class ThemeManager {

    public static final String MODE_SYSTEM = "system";
    public static final String MODE_LIGHT = "light";
    public static final String MODE_DARK = "dark";

    private static final String PREFS = "calmer_theme";
    private static final String KEY_MODE = "mode";

    private ThemeManager() {
    }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static String getMode(Context context) {
        return prefs(context).getString(KEY_MODE, MODE_SYSTEM);
    }

    /** Aplica o tema salvo. Chamado no start do app. */
    public static void applySaved(Context context) {
        apply(getMode(context));
    }

    /** Salva e aplica o modo escolhido (recria as telas). */
    public static void setMode(Context context, String mode) {
        prefs(context).edit().putString(KEY_MODE, mode).apply();
        apply(mode);
    }

    private static void apply(String mode) {
        AppCompatDelegate.setDefaultNightMode(toNightMode(mode));
    }

    private static int toNightMode(String mode) {
        switch (mode) {
            case MODE_LIGHT:
                return AppCompatDelegate.MODE_NIGHT_NO;
            case MODE_DARK:
                return AppCompatDelegate.MODE_NIGHT_YES;
            case MODE_SYSTEM:
            default:
                return AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        }
    }
}
