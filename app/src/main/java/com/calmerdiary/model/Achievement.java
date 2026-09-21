package com.calmerdiary.model;

import com.calmerdiary.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Definição de uma conquista. As conquistas são desbloqueadas automaticamente
 * conforme as estatísticas do usuário atingem a meta ({@link #target}).
 */
public class Achievement {

    /** Qual métrica do usuário essa conquista acompanha. */
    public enum Metric {
        TOTAL_ENTRIES,
        LONGEST_STREAK,
        DISTINCT_MOODS
    }

    public final String key;
    public final String emoji;
    public final int titleRes;
    public final int descRes;
    public final int target;
    public final Metric metric;

    public Achievement(String key, String emoji, int titleRes, int descRes, int target, Metric metric) {
        this.key = key;
        this.emoji = emoji;
        this.titleRes = titleRes;
        this.descRes = descRes;
        this.target = target;
        this.metric = metric;
    }

    /** Catálogo fixo de conquistas do app. */
    public static List<Achievement> catalog() {
        List<Achievement> list = new ArrayList<>();
        list.add(new Achievement("first_entry", "📖",
                R.string.ach_first_title, R.string.ach_first_desc, 1, Metric.TOTAL_ENTRIES));
        list.add(new Achievement("entries_10", "📚",
                R.string.ach_entries10_title, R.string.ach_entries10_desc, 10, Metric.TOTAL_ENTRIES));
        list.add(new Achievement("entries_30", "✍️",
                R.string.ach_entries30_title, R.string.ach_entries30_desc, 30, Metric.TOTAL_ENTRIES));
        list.add(new Achievement("streak_3", "🔥",
                R.string.ach_streak3_title, R.string.ach_streak3_desc, 3, Metric.LONGEST_STREAK));
        list.add(new Achievement("streak_7", "📅",
                R.string.ach_streak7_title, R.string.ach_streak7_desc, 7, Metric.LONGEST_STREAK));
        list.add(new Achievement("palette", "🎨",
                R.string.ach_palette_title, R.string.ach_palette_desc, 6, Metric.DISTINCT_MOODS));
        return list;
    }
}
