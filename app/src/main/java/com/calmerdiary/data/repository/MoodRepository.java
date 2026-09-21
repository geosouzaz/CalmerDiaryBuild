package com.calmerdiary.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.calmerdiary.data.database.AppDatabase;
import com.calmerdiary.data.dao.MoodDao;
import com.calmerdiary.data.entities.MoodEntry;
import com.calmerdiary.model.MoodCount;
import com.calmerdiary.util.AppExecutors;
import com.calmerdiary.util.Callback;

import java.util.List;

/**
 * Acesso aos registros de humor para estatísticas e estabilidade emocional.
 */
public class MoodRepository {

    private final MoodDao moodDao;
    private final AppExecutors executors;

    public MoodRepository(Context context) {
        this.moodDao = AppDatabase.getInstance(context).moodDao();
        this.executors = AppExecutors.getInstance();
    }

    public LiveData<List<MoodEntry>> observeInRange(long userId, long start, long end) {
        return moodDao.observeInRange(userId, start, end);
    }

    public void getCountsInRange(long userId, long start, long end, Callback<List<MoodCount>> callback) {
        executors.diskIO().execute(() -> {
            try {
                postSuccess(callback, moodDao.countByMoodInRange(userId, start, end));
            } catch (Exception e) {
                postError(callback, e);
            }
        });
    }

    public void getEntriesInRange(long userId, long start, long end, Callback<List<MoodEntry>> callback) {
        executors.diskIO().execute(() -> {
            try {
                postSuccess(callback, moodDao.getInRangeSync(userId, start, end));
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
