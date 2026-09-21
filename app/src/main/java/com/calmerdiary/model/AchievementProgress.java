package com.calmerdiary.model;

import java.util.ArrayList;
import java.util.List;

/** Progresso de uma conquista para um usuário (valor atual, meta, %). */
public class AchievementProgress {

    public final Achievement achievement;
    public final int current;
    public final boolean unlocked;
    public final int percent;

    public AchievementProgress(Achievement achievement, int current) {
        this.achievement = achievement;
        this.current = Math.min(current, achievement.target);
        this.unlocked = current >= achievement.target;
        this.percent = achievement.target == 0 ? 100
                : Math.min(100, Math.round(current * 100f / achievement.target));
    }

    /** Avalia todas as conquistas do catálogo para as estatísticas informadas. */
    public static List<AchievementProgress> evaluate(UserStats stats) {
        List<AchievementProgress> result = new ArrayList<>();
        for (Achievement a : Achievement.catalog()) {
            int current;
            switch (a.metric) {
                case LONGEST_STREAK:
                    current = stats.longestStreak;
                    break;
                case DISTINCT_MOODS:
                    current = stats.distinctMoods;
                    break;
                case TOTAL_ENTRIES:
                default:
                    current = stats.totalEntries;
                    break;
            }
            result.add(new AchievementProgress(a, current));
        }
        return result;
    }
}
