package com.calmerdiary.data.entities;

import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Registro de humor, usado para as estatísticas e a estabilidade emocional.
 * É mantido em espelho com a entrada do diário (um registro por entrada com
 * humor) e sincronizado pelo repositório dentro de uma transação.
 */
@Entity(
        tableName = "mood_entries",
        foreignKeys = {
                @ForeignKey(
                        entity = User.class,
                        parentColumns = "id",
                        childColumns = "userId",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = DiaryEntry.class,
                        parentColumns = "id",
                        childColumns = "diaryEntryId",
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = {@Index("userId"), @Index("diaryEntryId"), @Index("date")}
)
public class MoodEntry {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public long userId;

    /** Entrada de diário associada; nulo permite registro avulso de humor. */
    @Nullable
    public Long diaryEntryId;

    public long date;

    public int mood;

    public int intensity;
}
