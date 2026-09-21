package com.calmerdiary.data.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

/**
 * Preferências por usuário (aparência, segurança e comportamento do diário).
 */
@Entity(
        tableName = "app_settings",
        foreignKeys = @ForeignKey(
                entity = User.class,
                parentColumns = "id",
                childColumns = "userId",
                onDelete = ForeignKey.CASCADE
        )
)
public class AppSettings {

    /** Modo de tema seguindo o sistema. */
    public static final String THEME_MODE_SYSTEM = "system";
    public static final String THEME_MODE_LIGHT = "light";
    public static final String THEME_MODE_DARK = "dark";

    @PrimaryKey
    public long userId;

    /** "system" | "light" | "dark" */
    @NonNull
    public String themeMode = THEME_MODE_SYSTEM;

    /** Variação de cor escolhida (ex.: "wine", "wine_dark", "rose", "night"). */
    @NonNull
    public String selectedTheme = "wine";

    public boolean biometricEnabled = false;

    public boolean confirmDelete = true;

    public boolean notificationsEnabled = false;

    /** Cria as preferências padrão para um usuário. */
    public static AppSettings defaultsFor(long userId) {
        AppSettings s = new AppSettings();
        s.userId = userId;
        s.themeMode = THEME_MODE_SYSTEM;
        s.selectedTheme = "wine";
        s.biometricEnabled = false;
        s.confirmDelete = true;
        s.notificationsEnabled = false;
        return s;
    }
}
