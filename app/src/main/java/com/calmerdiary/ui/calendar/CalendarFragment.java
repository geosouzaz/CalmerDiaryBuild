package com.calmerdiary.ui.calendar;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.calmerdiary.databinding.FragmentCalendarBinding;
import com.calmerdiary.ui.diary.EntryAdapter;
import com.calmerdiary.ui.diary.EntryViewActivity;
import com.calmerdiary.util.DateUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Aba Calendário: grade do mês com dias marcados e entradas do dia. */
public class CalendarFragment extends Fragment {

    private static final Locale PT_BR = new Locale("pt", "BR");
    private static final String[] WEEK_LETTERS = {"D", "S", "T", "Q", "Q", "S", "S"};

    private FragmentCalendarBinding binding;
    private CalendarViewModel viewModel;
    private CalendarDayAdapter dayAdapter;

    // Últimos valores conhecidos para reconstruir a grade.
    private int[] monthYear;
    private final Set<Long> entryDays = new HashSet<>();
    private long selectedDate;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentCalendarBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(CalendarViewModel.class);

        buildWeekHeader();

        dayAdapter = new CalendarDayAdapter(viewModel::selectDate);
        binding.rvCalendar.setLayoutManager(new GridLayoutManager(requireContext(), 7));
        binding.rvCalendar.setAdapter(dayAdapter);

        EntryAdapter entryAdapter = new EntryAdapter(entry -> {
            Intent intent = new Intent(requireContext(), EntryViewActivity.class);
            intent.putExtra(EntryViewActivity.EXTRA_ENTRY_ID, entry.id);
            startActivity(intent);
        });
        binding.rvDayEntries.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvDayEntries.setAdapter(entryAdapter);

        binding.btnPrevMonth.setOnClickListener(v -> viewModel.prevMonth());
        binding.btnNextMonth.setOnClickListener(v -> viewModel.nextMonth());

        viewModel.getMonthYear().observe(getViewLifecycleOwner(), my -> {
            monthYear = my;
            updateMonthLabel();
            rebuildGrid();
        });

        viewModel.getSelectedDate().observe(getViewLifecycleOwner(), date -> {
            selectedDate = date == null ? 0L : date;
            rebuildGrid();
        });

        viewModel.getEntryDates().observe(getViewLifecycleOwner(), dates -> {
            entryDays.clear();
            if (dates != null) {
                for (Long d : dates) {
                    entryDays.add(DateUtils.startOfDay(d));
                }
            }
            rebuildGrid();
        });

        viewModel.getDayEntries().observe(getViewLifecycleOwner(), entries -> {
            boolean empty = entries == null || entries.isEmpty();
            binding.tvNoDayEntries.setVisibility(empty ? View.VISIBLE : View.GONE);
            binding.rvDayEntries.setVisibility(empty ? View.GONE : View.VISIBLE);
            entryAdapter.submitList(entries);
        });
    }

    private void buildWeekHeader() {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (String letter : WEEK_LETTERS) {
            TextView tv = new TextView(requireContext());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            tv.setLayoutParams(lp);
            tv.setGravity(Gravity.CENTER);
            tv.setText(letter);
            tv.setTextSize(13f);
            tv.setTextColor(getResources().getColor(com.calmerdiary.R.color.text_secondary_dark, null));
            binding.weekHeader.addView(tv);
        }
    }

    private void updateMonthLabel() {
        if (monthYear == null) {
            return;
        }
        Calendar c = Calendar.getInstance();
        c.clear();
        c.set(monthYear[0], monthYear[1], 1);
        String label = new SimpleDateFormat("MMMM 'de' yyyy", PT_BR).format(c.getTime());
        binding.tvMonth.setText(label.substring(0, 1).toUpperCase(PT_BR) + label.substring(1));
    }

    private void rebuildGrid() {
        if (monthYear == null) {
            return;
        }
        Calendar c = Calendar.getInstance();
        c.clear();
        c.set(monthYear[0], monthYear[1], 1);
        int offset = c.get(Calendar.DAY_OF_WEEK) - 1; // domingo = 0
        int daysInMonth = c.getActualMaximum(Calendar.DAY_OF_MONTH);

        List<CalendarDayAdapter.DayCell> cells = new ArrayList<>();
        for (int i = 0; i < offset; i++) {
            cells.add(new CalendarDayAdapter.DayCell(0, 0L, false, false));
        }
        for (int day = 1; day <= daysInMonth; day++) {
            Calendar d = Calendar.getInstance();
            d.clear();
            d.set(monthYear[0], monthYear[1], day);
            long millis = DateUtils.startOfDay(d.getTimeInMillis());
            boolean hasEntry = entryDays.contains(millis);
            boolean selected = selectedDate == millis;
            cells.add(new CalendarDayAdapter.DayCell(day, millis, hasEntry, selected));
        }
        dayAdapter.setCells(cells);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
