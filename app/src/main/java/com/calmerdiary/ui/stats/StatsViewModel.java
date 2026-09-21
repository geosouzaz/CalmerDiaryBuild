package com.calmerdiary.ui.stats;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.calmerdiary.data.entities.MoodEntry;
import com.calmerdiary.data.repository.MoodRepository;
import com.calmerdiary.util.DateUtils;
import com.calmerdiary.util.SessionManager;

import java.util.Calendar;
import java.util.List;

public class StatsViewModel extends AndroidViewModel {

    private final MoodRepository moodRepository;
    private final long userId;

    private final MutableLiveData<Integer> periodDays = new MutableLiveData<>(7);
    private final LiveData<List<MoodEntry>> moodEntries;

    public StatsViewModel(@NonNull Application application) {
        super(application);
        this.moodRepository = new MoodRepository(application);
        this.userId = new SessionManager(application).getUserId();

        this.moodEntries = Transformations.switchMap(periodDays, days -> {
            long now = System.currentTimeMillis();
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(now);
            c.add(Calendar.DAY_OF_MONTH, -(days - 1));
            long start = DateUtils.startOfDay(c.getTimeInMillis());
            long end = DateUtils.endOfDay(now);
            return moodRepository.observeInRange(userId, start, end);
        });
    }

    public LiveData<List<MoodEntry>> getMoodEntries() {
        return moodEntries;
    }

    public int getPeriodDays() {
        Integer d = periodDays.getValue();
        return d == null ? 7 : d;
    }

    public void setPeriodDays(int days) {
        periodDays.setValue(days);
    }
}
