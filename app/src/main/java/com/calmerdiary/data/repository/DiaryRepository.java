package com.calmerdiary.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.calmerdiary.data.database.AppDatabase;
import com.calmerdiary.data.dao.DiaryDao;
import com.calmerdiary.data.dao.MoodDao;
import com.calmerdiary.data.entities.DiaryEntry;
import com.calmerdiary.data.entities.MoodEntry;
import com.calmerdiary.util.AppExecutors;
import com.calmerdiary.util.Callback;

import java.util.List;

/**
 * CRUD das entradas do diário. Mantém o espelho de humor (MoodEntry)
 * sincronizado dentro de uma transação.
 */
public class DiaryRepository {

    private final AppDatabase db;
    private final DiaryDao diaryDao;
    private final MoodDao moodDao;
    private final AppExecutors executors;

    public DiaryRepository(Context context) {
        this.db = AppDatabase.getInstance(context);
        this.diaryDao = db.diaryDao();
        this.moodDao = db.moodDao();
        this.executors = AppExecutors.getInstance();
    }

    // ---------- Observáveis (para a camada ViewModel) ----------

    public LiveData<List<DiaryEntry>> observeEntries(long userId) {
        return diaryDao.observeAllForUser(userId);
    }

    public LiveData<DiaryEntry> observeEntry(long id, long userId) {
        return diaryDao.observeByIdForUser(id, userId);
    }

    public LiveData<List<DiaryEntry>> search(long userId, String query) {
        return diaryDao.search(userId, query);
    }

    public LiveData<List<DiaryEntry>> observeInRange(long userId, long start, long end) {
        return diaryDao.observeInRange(userId, start, end);
    }

    public LiveData<List<DiaryEntry>> searchInRange(long userId, String query, long start, long end) {
        return diaryDao.searchInRange(userId, query, start, end);
    }

    public LiveData<Integer> observeCount(long userId) {
        return diaryDao.observeCountForUser(userId);
    }

    public LiveData<List<Long>> observeEntryDates(long userId) {
        return diaryDao.observeEntryDates(userId);
    }

    // ---------- Escrita ----------

    /**
     * Insere (id == 0) ou atualiza a entrada, mantendo o registro de humor.
     * Retorna o id da entrada.
     */
    public void save(DiaryEntry entry, Callback<Long> callback) {
        executors.diskIO().execute(() -> {
            try {
                db.runInTransaction(() -> {
                    long now = System.currentTimeMillis();
                    if (entry.id == 0) {
                        entry.createdAt = now;
                        entry.updatedAt = now;
                        entry.id = diaryDao.insert(entry);
                    } else {
                        entry.updatedAt = now;
                        diaryDao.update(entry);
                        moodDao.deleteByDiaryEntry(entry.id);
                    }
                    if (entry.mood >= 0) {
                        MoodEntry mood = new MoodEntry();
                        mood.userId = entry.userId;
                        mood.diaryEntryId = entry.id;
                        mood.date = entry.date;
                        mood.mood = entry.mood;
                        mood.intensity = entry.moodIntensity;
                        moodDao.insert(mood);
                    }
                });
                postSuccess(callback, entry.id);
            } catch (Exception e) {
                postError(callback, e);
            }
        });
    }

    public void delete(DiaryEntry entry, Callback<Integer> callback) {
        executors.diskIO().execute(() -> {
            try {
                int rows = diaryDao.delete(entry); // MoodEntry removido por CASCADE
                postSuccess(callback, rows);
            } catch (Exception e) {
                postError(callback, e);
            }
        });
    }

    public void getEntry(long id, long userId, Callback<DiaryEntry> callback) {
        executors.diskIO().execute(() -> {
            try {
                postSuccess(callback, diaryDao.findByIdForUser(id, userId));
            } catch (Exception e) {
                postError(callback, e);
            }
        });
    }

    public void getEntryDates(long userId, Callback<List<Long>> callback) {
        executors.diskIO().execute(() -> {
            try {
                postSuccess(callback, diaryDao.getEntryDates(userId));
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
