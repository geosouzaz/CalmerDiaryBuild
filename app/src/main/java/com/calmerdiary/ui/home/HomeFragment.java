package com.calmerdiary.ui.home;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.calmerdiary.R;
import com.calmerdiary.databinding.FragmentHomeBinding;
import com.calmerdiary.ui.diary.EntryAdapter;
import com.calmerdiary.ui.diary.EntryViewActivity;
import com.calmerdiary.util.DateUtils;
import com.calmerdiary.util.StreakUtils;

import android.content.Intent;

import java.util.Calendar;
import java.util.List;

/** Aba principal (Diário): saudação, sequência e resumo. */
public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private HomeViewModel viewModel;
    private EntryAdapter adapter;

    private static final String[] DAY_LETTERS = {"D", "S", "T", "Q", "Q", "S", "S"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        binding.tvGreeting.setText(getString(R.string.home_greeting, viewModel.getUserName()));

        adapter = new EntryAdapter(entry -> openViewer(entry.id));
        binding.rvEntries.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvEntries.setAdapter(adapter);

        viewModel.getTotalEntries().observe(getViewLifecycleOwner(), total ->
                binding.tvTotalEntries.setText(String.valueOf(total == null ? 0 : total)));

        viewModel.getEntries().observe(getViewLifecycleOwner(), entries -> {
            boolean empty = entries == null || entries.isEmpty();
            binding.emptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
            binding.rvEntries.setVisibility(empty ? View.GONE : View.VISIBLE);
            if (empty) {
                boolean filtering = viewModel.isFiltering();
                binding.tvEmptyTitle.setText(filtering
                        ? R.string.empty_search_title : R.string.empty_entries_title);
                binding.tvEmptyHint.setText(filtering
                        ? R.string.empty_search_hint : R.string.empty_entries_hint);
            }
            adapter.submitList(entries);
        });

        viewModel.getEntryDates().observe(getViewLifecycleOwner(), this::bindStreak);

        setupSearchAndFilters();
    }

    private void setupSearchAndFilters() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.setQuery(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        binding.chipGroupFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            HomeViewModel.Period period = HomeViewModel.Period.ALL;
            if (!checkedIds.isEmpty()) {
                int id = checkedIds.get(0);
                if (id == R.id.chipToday) {
                    period = HomeViewModel.Period.TODAY;
                } else if (id == R.id.chipWeek) {
                    period = HomeViewModel.Period.WEEK;
                } else if (id == R.id.chipMonth) {
                    period = HomeViewModel.Period.MONTH;
                } else if (id == R.id.chipYear) {
                    period = HomeViewModel.Period.YEAR;
                }
            }
            viewModel.setPeriod(period);
        });
    }

    private void openViewer(long entryId) {
        Intent intent = new Intent(requireContext(), EntryViewActivity.class);
        intent.putExtra(EntryViewActivity.EXTRA_ENTRY_ID, entryId);
        startActivity(intent);
    }

    private void bindStreak(List<Long> dates) {
        int current = StreakUtils.currentStreak(dates);
        int longest = StreakUtils.longestStreak(dates);
        int active = StreakUtils.activeDays(dates);

        binding.tvStreakTitle.setText("🔥 " + getString(R.string.streak_title, current));
        binding.tvActiveDays.setText(String.valueOf(active));
        binding.tvLongestStreak.setText(String.valueOf(longest));

        buildLast7Days(dates);
    }

    /** Monta a régua dos últimos 7 dias, marcando os dias com entrada. */
    private void buildLast7Days(List<Long> dates) {
        binding.streakRow.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(requireContext());

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -6); // começa 6 dias atrás até hoje

        for (int i = 0; i < 7; i++) {
            long dayStart = DateUtils.startOfDay(cal.getTimeInMillis());

            View cell = inflater.inflate(R.layout.item_streak_day, binding.streakRow, false);
            TextView letter = cell.findViewById(R.id.tvDayLetter);
            TextView number = cell.findViewById(R.id.tvDayNumber);

            int dow = cal.get(Calendar.DAY_OF_WEEK); // 1=Domingo .. 7=Sábado
            letter.setText(DAY_LETTERS[dow - 1]);
            number.setText(String.valueOf(cal.get(Calendar.DAY_OF_MONTH)));

            if (StreakUtils.hasEntryOn(dates, dayStart)) {
                number.setBackgroundResource(R.drawable.bg_streak_day_active);
            } else {
                number.setBackgroundResource(R.drawable.bg_streak_day_inactive);
            }

            binding.streakRow.addView(cell);
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
