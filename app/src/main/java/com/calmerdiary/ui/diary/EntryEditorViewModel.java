package com.calmerdiary.ui.diary;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.calmerdiary.data.entities.DiaryEntry;
import com.calmerdiary.data.repository.DiaryRepository;
import com.calmerdiary.util.Callback;
import com.calmerdiary.util.Event;
import com.calmerdiary.util.SessionManager;

public class EntryEditorViewModel extends AndroidViewModel {

    private final DiaryRepository diaryRepository;
    private final long userId;

    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<Event<Long>> saved = new MutableLiveData<>();
    private final MutableLiveData<Event<String>> error = new MutableLiveData<>();
    private final MutableLiveData<DiaryEntry> loadedEntry = new MutableLiveData<>();

    public EntryEditorViewModel(@NonNull Application application) {
        super(application);
        this.diaryRepository = new DiaryRepository(application);
        this.userId = new SessionManager(application).getUserId();
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<Event<Long>> getSaved() {
        return saved;
    }

    public LiveData<Event<String>> getError() {
        return error;
    }

    public LiveData<DiaryEntry> getLoadedEntry() {
        return loadedEntry;
    }

    public long getUserId() {
        return userId;
    }

    public void loadEntry(long entryId) {
        diaryRepository.getEntry(entryId, userId, new Callback<DiaryEntry>() {
            @Override
            public void onSuccess(DiaryEntry result) {
                loadedEntry.setValue(result);
            }

            @Override
            public void onError(Exception e) {
                error.setValue(new Event<>(e.getMessage()));
            }
        });
    }

    public void save(DiaryEntry entry) {
        entry.userId = userId;
        loading.setValue(true);
        diaryRepository.save(entry, new Callback<Long>() {
            @Override
            public void onSuccess(Long id) {
                loading.setValue(false);
                saved.setValue(new Event<>(id));
            }

            @Override
            public void onError(Exception e) {
                loading.setValue(false);
                error.setValue(new Event<>(e.getMessage()));
            }
        });
    }
}
