package com.calmerdiary.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.calmerdiary.data.database.AppDatabase;
import com.calmerdiary.data.dao.SettingsDao;
import com.calmerdiary.data.entities.AppSettings;
import com.calmerdiary.util.AppExecutors;
import com.calmerdiary.util.Callback;

/**
 * Preferências do usuário (tema, biometria, confirmação de exclusão, etc.).
 */
public class SettingsRepository {

    private final SettingsDao settingsDao;
    private final AppExecutors executors;

    public SettingsRepository(Context context) {
        this.settingsDao = AppDatabase.getInstance(context).settingsDao();
        this.executors = AppExecutors.getInstance();
    }

    public LiveData<AppSettings> observeSettings(long userId) {
        return settingsDao.observeForUser(userId);
    }

    public void getSettings(long userId, Callback<AppSettings> callback) {
        executors.diskIO().execute(() -> {
            try {
                AppSettings settings = settingsDao.getForUser(userId);
                if (settings == null) {
                    settings = AppSettings.defaultsFor(userId);
                    settingsDao.upsert(settings);
                }
                postSuccess(callback, settings);
            } catch (Exception e) {
                postError(callback, e);
            }
        });
    }

    public void save(AppSettings settings, Callback<Void> callback) {
        executors.diskIO().execute(() -> {
            try {
                settingsDao.upsert(settings);
                postSuccess(callback, null);
            } catch (Exception e) {
                postError(callback, e);
            }
        });
    }

    private <T> void postSuccess(Callback<T> callback, T result) {
        if (callback != null) {
            executors.mainThread().execute(() -> callback.onSuccess(result));
        }
    }

    private <T> void postError(Callback<T> callback, Exception error) {
        if (callback != null) {
            executors.mainThread().execute(() -> callback.onError(error));
        }
    }
}
