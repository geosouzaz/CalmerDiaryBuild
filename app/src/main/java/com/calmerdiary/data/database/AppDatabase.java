package com.calmerdiary.data.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.calmerdiary.data.dao.DiaryDao;
import com.calmerdiary.data.dao.MoodDao;
import com.calmerdiary.data.dao.SettingsDao;
import com.calmerdiary.data.dao.UserDao;
import com.calmerdiary.data.entities.AppSettings;
import com.calmerdiary.data.entities.DiaryEntry;
import com.calmerdiary.data.entities.MoodEntry;
import com.calmerdiary.data.entities.User;

/**
 * Banco de dados local (Room). Ponto único de acesso à persistência.
 * As restrições de chave estrangeira são habilitadas pelo Room, garantindo
 * a exclusão em cascata das entradas/humores ao remover um usuário.
 */
@Database(
        entities = {User.class, DiaryEntry.class, MoodEntry.class, AppSettings.class},
        version = 1,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    private static final String DB_NAME = "calmer_diary.db";

    private static volatile AppDatabase instance;

    public abstract UserDao userDao();

    public abstract DiaryDao diaryDao();

    public abstract MoodDao moodDao();

    public abstract SettingsDao settingsDao();

    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    DB_NAME)
                            .build();
                }
            }
        }
        return instance;
    }
}
