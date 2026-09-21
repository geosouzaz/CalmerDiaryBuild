package com.calmerdiary.data.entities;

import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Uma entrada do diário, sempre vinculada a um usuário.
 * A exclusão do usuário remove suas entradas (CASCADE), garantindo
 * o isolamento e a privacidade dos dados.
 */
@Entity(
        tableName = "diary_entries",
        foreignKeys = @ForeignKey(
                entity = User.class,
                parentColumns = "id",
                childColumns = "userId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("userId"), @Index("date")}
)
public class DiaryEntry {

    @PrimaryKey(autoGenerate = true)
    public long id;

    /** Dono da entrada. */
    public long userId;

    public String title;

    public String content;

    /** Data da entrada (epoch millis, normalizada para o início do dia local). */
    public long date;

    /** Índice do humor selecionado; -1 quando nenhum. */
    public int mood = -1;

    /** Intensidade do humor (1..5); 0 quando não informado. */
    public int moodIntensity;

    /** URI de uma imagem opcional da galeria (permissão persistente). */
    @Nullable
    public String imageUri;

    public long createdAt;

    public long updatedAt;
}
