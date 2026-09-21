package com.calmerdiary;

import android.app.Application;

import com.calmerdiary.util.ThemeManager;

/**
 * Classe Application do Calmer Diary.
 * Aplica o tema escolhido (claro/escuro/sistema) já no início.
 */
public class CalmerApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ThemeManager.applySaved(this);
    }
}
