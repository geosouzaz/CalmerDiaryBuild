package com.calmerdiary.util;

import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * Cálculos de sequência (streak) a partir das datas com entradas.
 * As datas são normalizadas para o início do dia local.
 */
public final class StreakUtils {

    private StreakUtils() {
    }

    private static Set<Long> normalize(Collection<Long> dates) {
        Set<Long> days = new HashSet<>();
        if (dates != null) {
            for (Long d : dates) {
                if (d != null) {
                    days.add(DateUtils.startOfDay(d));
                }
            }
        }
        return days;
    }

    private static long minusOneDay(long dayStart) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(dayStart);
        c.add(Calendar.DAY_OF_MONTH, -1);
        return DateUtils.startOfDay(c.getTimeInMillis());
    }

    private static long plusOneDay(long dayStart) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(dayStart);
        c.add(Calendar.DAY_OF_MONTH, 1);
        return DateUtils.startOfDay(c.getTimeInMillis());
    }

    /** Sequência atual: dias consecutivos com entrada terminando hoje (ou ontem). */
    public static int currentStreak(Collection<Long> dates) {
        Set<Long> days = normalize(dates);
        if (days.isEmpty()) {
            return 0;
        }
        long today = DateUtils.startOfDay(System.currentTimeMillis());
        long anchor;
        if (days.contains(today)) {
            anchor = today;
        } else {
            long yesterday = minusOneDay(today);
            if (days.contains(yesterday)) {
                anchor = yesterday;
            } else {
                return 0;
            }
        }
        int streak = 0;
        long cursor = anchor;
        while (days.contains(cursor)) {
            streak++;
            cursor = minusOneDay(cursor);
        }
        return streak;
    }

    /** Maior sequência já registrada. */
    public static int longestStreak(Collection<Long> dates) {
        Set<Long> daysSet = normalize(dates);
        if (daysSet.isEmpty()) {
            return 0;
        }
        TreeSet<Long> ordered = new TreeSet<>(daysSet);
        int longest = 1;
        int run = 1;
        Long prev = null;
        for (Long day : ordered) {
            if (prev != null && day == plusOneDay(prev)) {
                run++;
            } else if (prev != null) {
                run = 1;
            }
            longest = Math.max(longest, run);
            prev = day;
        }
        return longest;
    }

    /** Total de dias com ao menos uma entrada. */
    public static int activeDays(Collection<Long> dates) {
        return normalize(dates).size();
    }

    /** Indica se um determinado dia possui entrada. */
    public static boolean hasEntryOn(Collection<Long> dates, long dayStart) {
        return normalize(dates).contains(DateUtils.startOfDay(dayStart));
    }
}
