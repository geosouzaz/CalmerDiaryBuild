package com.calmerdiary.model;

import androidx.annotation.Nullable;

import com.calmerdiary.R;

/**
 * Humores disponíveis para uma entrada. O índice ({@link #index}) é o valor
 * persistido em {@code DiaryEntry.mood} (-1 significa nenhum humor).
 */
public enum Mood {

    VERY_HAPPY(0, "😄", R.string.mood_very_happy),
    HAPPY(1, "🙂", R.string.mood_happy),
    IN_LOVE(2, "😍", R.string.mood_in_love),
    CALM(3, "😌", R.string.mood_calm),
    NEUTRAL(4, "😐", R.string.mood_neutral),
    SAD(5, "😔", R.string.mood_sad),
    ANXIOUS(6, "😰", R.string.mood_anxious),
    ANGRY(7, "😡", R.string.mood_angry);

    public final int index;
    public final String emoji;
    public final int labelRes;

    Mood(int index, String emoji, int labelRes) {
        this.index = index;
        this.emoji = emoji;
        this.labelRes = labelRes;
    }

    public static Mood[] all() {
        return values();
    }

    @Nullable
    public static Mood fromIndex(int index) {
        for (Mood mood : values()) {
            if (mood.index == index) {
                return mood;
            }
        }
        return null;
    }

    /** Emoji do índice informado, ou vazio quando não há humor. */
    public static String emojiOf(int index) {
        Mood mood = fromIndex(index);
        return mood == null ? "" : mood.emoji;
    }
}
