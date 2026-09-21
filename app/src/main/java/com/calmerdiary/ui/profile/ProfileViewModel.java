package com.calmerdiary.ui.profile;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Transformations;

import com.calmerdiary.data.entities.MoodEntry;
import com.calmerdiary.data.repository.DiaryRepository;
import com.calmerdiary.data.repository.MoodRepository;
import com.calmerdiary.model.AchievementProgress;
import com.calmerdiary.model.UserStats;
import com.calmerdiary.util.SessionManager;
import com.calmerdiary.util.StreakUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProfileViewModel extends AndroidViewModel {

    private final SessionManager session;

    private final LiveData<Integer> totalEntries;
    private final LiveData<List<Long>> entryDates;
    private final LiveData<List<MoodEntry>> moodEntries;

    private final MediatorLiveData<UserStats> stats = new MediatorLiveData<>();
    private final LiveData<List<AchievementProgress>> achievements;

    public ProfileViewModel(@NonNull Application application) {
        super(application);
        this.session = new SessionManager(application);
        long userId = session.getUserId();

        DiaryRepository diaryRepository = new DiaryRepository(application);
        MoodRepository moodRepository = new MoodRepository(application);

        this.totalEntries = diaryRepository.observeCount(userId);
        this.entryDates = diaryRepository.observeEntryDates(userId);
        this.moodEntries = moodRepository.observeInRange(userId, 0L, Long.MAX_VALUE);

        stats.addSource(totalEntries, v -> recompute());
        stats.addSource(entryDates, v -> recompute());
        stats.addSource(moodEntries, v -> recompute());

        this.achievements = Transformations.map(stats, AchievementProgress::evaluate);
    }

    private void recompute() {
        UserStats s = new UserStats();
        Integer total = totalEntries.getValue();
        s.totalEntries = total == null ? 0 : total;

        List<Long> dates = entryDates.getValue();
        s.activeDays = StreakUtils.activeDays(dates);
        s.currentStreak = StreakUtils.currentStreak(dates);
        s.longestStreak = StreakUtils.longestStreak(dates);

        Set<Integer> moods = new HashSet<>();
        List<MoodEntry> entries = moodEntries.getValue();
        if (entries != null) {
            for (MoodEntry e : entries) {
                moods.add(e.mood);
            }
        }
        s.distinctMoods = moods.size();

        stats.setValue(s);
    }

    public LiveData<UserStats> getStats() {
        return stats;
    }

    public LiveData<List<AchievementProgress>> getAchievements() {
        return achievements;
    }

    public String getUserName() {
        return session.getUserName();
    }

    public String getUserEmail() {
        return session.getUserEmail();
    }
}
