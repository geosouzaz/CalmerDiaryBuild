package com.calmerdiary.ui.diary;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.calmerdiary.data.entities.DiaryEntry;
import com.calmerdiary.data.repository.DiaryRepository;
import com.calmerdiary.data.repository.SettingsRepository;
import com.calmerdiary.util.Callback;
import com.calmerdiary.util.Event;

public class EntryViewViewModel extends AndroidViewModel {

    private final DiaryRepository diaryRepository;
    private final SettingsRepository settingsRepository;
    private final long userId;

    private LiveData<DiaryEntry> entry;
    private final MutableLiveData<Event<Boolean>> deleted = new MutableLiveData<>();

    public EntryViewViewModel(@NonNull Application application) {
        super(application);
        this.diaryRepository = new DiaryRepository(application);
        this.settingsRepository = new SettingsRepository(application);
        this.userId = new com.calmerdiary.util.SessionManager(application).getUserId();
    }

    public LiveData<DiaryEntry> observe(long entryId) {
        if (entry == null) {
            entry = diaryRepository.observeEntry(entryId, userId);
        }
        return entry;
    }

    public LiveData<Event<Boolean>> getDeleted() {
        return deleted;
    }

    public void isConfirmDeleteEnabled(Callback<Boolean> callback) {
        settingsRepository.getSettings(userId, new Callback<com.calmerdiary.data.entities.AppSettings>() {
            @Override
            public void onSuccess(com.calmerdiary.data.entities.AppSettings settings) {
                callback.onSuccess(settings == null || settings.confirmDelete);
            }

            @Override
            public void onError(Exception e) {
                callback.onSuccess(true); // por segurança, confirma
            }
        });
    }

    public void delete(DiaryEntry entryToDelete) {
        diaryRepository.delete(entryToDelete, new Callback<Integer>() {
            @Override
            public void onSuccess(Integer rows) {
                deleted.setValue(new Event<>(true));
            }

            @Override
            public void onError(Exception e) {
                deleted.setValue(new Event<>(false));
            }
        });
    }
}
