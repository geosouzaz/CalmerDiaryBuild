package com.calmerdiary.ui.home;

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

public class HomeViewModel extends AndroidViewModel {

    /** Períodos de filtro da lista. */
    public enum Period {
        ALL, TODAY, WEEK, MONTH, YEAR
    }

    /** Combinação de busca + período aplicada à lista. */
    private static class Filter {
        final String query;
        final Period period;

        Filter(String query, Period period) {
            this.query = query;
            this.period = period;
        }
    }

    private final DiaryRepository diaryRepository;
    private final SessionManager session;
    private final long userId;

    private final LiveData<Integer> totalEntries;
    private final LiveData<List<Long>> entryDates;

    private final MutableLiveData<Filter> filter = new MutableLiveData<>(new Filter("", Period.ALL));
    private final LiveData<List<DiaryEntry>> entries;

    public HomeViewModel(@NonNull Application application) {
        super(application);
        this.diaryRepository = new DiaryRepository(application);
        this.session = new SessionManager(application);
        this.userId = session.getUserId();
        this.totalEntries = diaryRepository.observeCount(userId);
        this.entryDates = diaryRepository.observeEntryDates(userId);

        this.entries = Transformations.switchMap(filter, f -> {
            long start = startOf(f.period);
            long end = endOf(f.period);
            return diaryRepository.searchInRange(userId, f.query == null ? "" : f.query, start, end);
        });
    }

    public LiveData<Integer> getTotalEntries() {
        return totalEntries;
    }

    public LiveData<List<Long>> getEntryDates() {
        return entryDates;
    }

    public LiveData<List<DiaryEntry>> getEntries() {
        return entries;
    }

    public String getUserName() {
        return session.getUserName();
    }

    public void setQuery(String query) {
        Filter current = filter.getValue();
        Period period = current == null ? Period.ALL : current.period;
        filter.setValue(new Filter(query, period));
    }

    public void setPeriod(Period period) {
        Filter current = filter.getValue();
        String query = current == null ? "" : current.query;
        filter.setValue(new Filter(query, period));
    }

    public boolean isFiltering() {
        Filter f = filter.getValue();
        return f != null && ((f.query != null && !f.query.isEmpty()) || f.period != Period.ALL);
    }

    // ---- Cálculo de intervalos por período ----

    private long startOf(Period period) {
        long now = System.currentTimeMillis();
        Calendar c = Calendar.getInstance();
        switch (period) {
            case TODAY:
                return DateUtils.startOfDay(now);
            case WEEK:
                c.setTimeInMillis(now);
                c.add(Calendar.DAY_OF_MONTH, -6);
                return DateUtils.startOfDay(c.getTimeInMillis());
            case MONTH:
                c.setTimeInMillis(now);
                return DateUtils.startOfMonth(c.get(Calendar.YEAR), c.get(Calendar.MONTH));
            case YEAR:
                c.setTimeInMillis(now);
                return DateUtils.startOfYear(c.get(Calendar.YEAR));
            case ALL:
            default:
                return 0L;
        }
    }

    private long endOf(Period period) {
        long now = System.currentTimeMillis();
        Calendar c = Calendar.getInstance();
        switch (period) {
            case TODAY:
            case WEEK:
                return DateUtils.endOfDay(now);
            case MONTH:
                c.setTimeInMillis(now);
                return DateUtils.endOfMonth(c.get(Calendar.YEAR), c.get(Calendar.MONTH));
            case YEAR:
                c.setTimeInMillis(now);
                return DateUtils.endOfYear(c.get(Calendar.YEAR));
            case ALL:
            default:
                return Long.MAX_VALUE;
        }
    }
}
