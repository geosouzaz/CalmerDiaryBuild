package com.calmerdiary.util;

import java.util.Calendar;

/**
 * Utilitários de data. As datas são guardadas como epoch millis normalizados
 * para o início do dia local, o que facilita filtros por dia/mês/ano.
 */
public final class DateUtils {

    private DateUtils() {
    }

    /** Início do dia (00:00:00.000) para o instante informado. */
    public static long startOfDay(long millis) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(millis);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    /** Fim do dia (23:59:59.999) para o instante informado. */
    public static long endOfDay(long millis) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(startOfDay(millis));
        c.add(Calendar.DAY_OF_MONTH, 1);
        return c.getTimeInMillis() - 1;
    }

    public static long startOfMonth(int year, int monthZeroBased) {
        Calendar c = Calendar.getInstance();
        c.clear();
        c.set(year, monthZeroBased, 1, 0, 0, 0);
        return c.getTimeInMillis();
    }

    public static long endOfMonth(int year, int monthZeroBased) {
        Calendar c = Calendar.getInstance();
        c.clear();
        c.set(year, monthZeroBased, 1, 0, 0, 0);
        c.add(Calendar.MONTH, 1);
        return c.getTimeInMillis() - 1;
    }

    public static long startOfYear(int year) {
        Calendar c = Calendar.getInstance();
        c.clear();
        c.set(year, Calendar.JANUARY, 1, 0, 0, 0);
        return c.getTimeInMillis();
    }

    public static long endOfYear(int year) {
        return startOfYear(year + 1) - 1;
    }

    /** Diferença em dias inteiros entre dois instantes (início de dia). */
    public static int daysBetween(long fromMillis, long toMillis) {
        long a = startOfDay(fromMillis);
        long b = startOfDay(toMillis);
        return (int) ((b - a) / (24L * 60 * 60 * 1000));
    }

    /** Verifica se dois instantes caem no mesmo dia local. */
    public static boolean isSameDay(long a, long b) {
        return startOfDay(a) == startOfDay(b);
    }
}
