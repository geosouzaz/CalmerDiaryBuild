package com.calmerdiary.ui.stats;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.calmerdiary.R;
import com.calmerdiary.data.entities.MoodEntry;
import com.calmerdiary.databinding.FragmentStatsBinding;
import com.calmerdiary.model.Mood;
import com.calmerdiary.util.DateUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/** Aba Estatísticas: contagem por humor, resumo Bem/Normal/Ruim e estabilidade. */
public class StatsFragment extends Fragment {

    private static final int[] MOOD_COLORS = {
            Color.parseColor("#E75480"), // Muito feliz
            Color.parseColor("#F2B705"), // Feliz
            Color.parseColor("#FF6F91"), // Apaixonado
            Color.parseColor("#8FBF8F"), // Calmo
            Color.parseColor("#A0A0A0"), // Normal
            Color.parseColor("#6FA8DC"), // Triste
            Color.parseColor("#9575CD"), // Ansioso
            Color.parseColor("#C0392B")  // Irritado
    };
    private static final String[] DAY_LETTERS = {"D", "S", "T", "Q", "Q", "S", "S"};

    private FragmentStatsBinding binding;
    private StatsViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentStatsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(StatsViewModel.class);

        int textColor = Color.WHITE;
        int mutedColor = getResources().getColor(R.color.text_secondary_dark, null);
        binding.chartMood.setColors(textColor, mutedColor);
        binding.chartStability.setColors(textColor, mutedColor);

        // Período inicial = semana
        binding.togglePeriod.check(R.id.btnWeek);
        binding.togglePeriod.addOnButtonCheckedListener((group, id, isChecked) -> {
            if (!isChecked) {
                return;
            }
            viewModel.setPeriodDays(id == R.id.btnMonth ? 30 : 7);
        });

        viewModel.getMoodEntries().observe(getViewLifecycleOwner(), this::render);
    }

    private void render(List<MoodEntry> entries) {
        boolean empty = entries == null || entries.isEmpty();
        binding.tvEmptyStats.setVisibility(empty ? View.VISIBLE : View.GONE);

        int[] counts = new int[8];
        if (entries != null) {
            for (MoodEntry e : entries) {
                if (e.mood >= 0 && e.mood < 8) {
                    counts[e.mood]++;
                }
            }
        }

        // Gráfico por humor
        List<BarChartView.Bar> moodBars = new ArrayList<>();
        int maxCount = 1;
        Mood[] moods = Mood.all();
        for (int i = 0; i < moods.length; i++) {
            moodBars.add(new BarChartView.Bar(moods[i].emoji, counts[i], MOOD_COLORS[i]));
            maxCount = Math.max(maxCount, counts[i]);
        }
        binding.chartMood.setData(moodBars, maxCount, false);

        // Resumo Bem / Normal / Ruim
        int bem = counts[0] + counts[1] + counts[2] + counts[3];
        int normal = counts[4];
        int ruim = counts[5] + counts[6] + counts[7];
        binding.tvBem.setText(String.valueOf(bem));
        binding.tvNormal.setText(String.valueOf(normal));
        binding.tvRuim.setText(String.valueOf(ruim));

        // Estabilidade emocional (últimos 7 dias)
        binding.chartStability.setData(buildStability(entries), 100f, true);
    }

    private List<BarChartView.Bar> buildStability(List<MoodEntry> entries) {
        int wineColor = getResources().getColor(R.color.wine_accent, null);
        List<BarChartView.Bar> bars = new ArrayList<>();

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -6);

        for (int i = 0; i < 7; i++) {
            long dayStart = DateUtils.startOfDay(cal.getTimeInMillis());
            long dayEnd = DateUtils.endOfDay(dayStart);

            float sum = 0f;
            int n = 0;
            if (entries != null) {
                for (MoodEntry e : entries) {
                    if (e.date >= dayStart && e.date <= dayEnd) {
                        sum += positivity(e.mood);
                        n++;
                    }
                }
            }
            float value = n == 0 ? 0f : (sum / n) * 100f;
            int dow = cal.get(Calendar.DAY_OF_WEEK);
            bars.add(new BarChartView.Bar(DAY_LETTERS[dow - 1], value, wineColor));

            cal.add(Calendar.DAY_OF_MONTH, 1);
        }
        return bars;
    }

    /** Positividade do humor: positivo=1, neutro=0.5, negativo=0. */
    private float positivity(int mood) {
        if (mood <= 3) {
            return 1f;      // muito feliz, feliz, apaixonado, calmo
        } else if (mood == 4) {
            return 0.5f;    // normal
        } else {
            return 0f;      // triste, ansioso, irritado
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
