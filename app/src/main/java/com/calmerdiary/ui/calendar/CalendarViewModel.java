package com.calmerdiary.ui.calendar;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.calmerdiary.data.entities.DiaryEntry;
import com.calmerdiary.data.repository.DiaryRepository;
import com.calmerdiary.util.DateUtils;
import com.calmerdiary.util.SessionManager;

import java.util.Calendar;
import java.util.List;

public class CalendarViewModel extends AndroidViewModel {

    private final DiaryRepository diaryRepository;
    private final long userId;

    private final MutableLiveData<int[]> monthYear = new MutableLiveData<>();
    private final MutableLiveData<Long> selectedDate = new MutableLiveData<>();
    private final LiveData<List<Long>> entryDates;
    private final LiveData<List<DiaryEntry>> dayEntries;

    public CalendarViewModel(@NonNull Application application) {
        super(application);
        this.diaryRepository = new DiaryRepository(application);
        this.userId = new SessionManager(application).getUserId();

        Calendar now = Calendar.getInstance();
        monthYear.setValue(new int[]{now.get(Calendar.YEAR), now.get(Calendar.MONTH)});
        selectedDate.setValue(DateUtils.startOfDay(now.getTimeInMillis()));

        this.entryDates = diaryRepository.observeEntryDates(userId);
        this.dayEntries = Transformations.switchMap(selectedDate, date ->
                diaryRepository.observeInRange(userId,
                        DateUtils.startOfDay(date), DateUtils.endOfDay(date)));
    }

    public LiveData<int[]> getMonthYear() {
        return monthYear;
    }

    public LiveData<Long> getSelectedDate() {
        return selectedDate;
    }

    public LiveData<List<Long>> getEntryDates() {
        return entryDates;
    }

    public LiveData<List<DiaryEntry>> getDayEntries() {
        return dayEntries;
    }

    public void selectDate(long millis) {
        selectedDate.setValue(DateUtils.startOfDay(millis));
    }

    public void prevMonth() {
        shiftMonth(-1);
    }

    public void nextMonth() {
        shiftMonth(1);
    }

    private void shiftMonth(int delta) {
        int[] my = monthYear.getValue();
        if (my == null) {
            return;
        }
        Calendar c = Calendar.getInstance();
        c.clear();
        c.set(my[0], my[1], 1);
        c.add(Calendar.MONTH, delta);
        monthYear.setValue(new int[]{c.get(Calendar.YEAR), c.get(Calendar.MONTH)});
    }
}
