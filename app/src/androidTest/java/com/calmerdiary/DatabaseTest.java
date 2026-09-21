package com.calmerdiary;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.calmerdiary.data.dao.DiaryDao;
import com.calmerdiary.data.dao.MoodDao;
import com.calmerdiary.data.dao.SettingsDao;
import com.calmerdiary.data.dao.UserDao;
import com.calmerdiary.data.database.AppDatabase;
import com.calmerdiary.data.entities.AppSettings;
import com.calmerdiary.data.entities.DiaryEntry;
import com.calmerdiary.data.entities.MoodEntry;
import com.calmerdiary.data.entities.User;
import com.calmerdiary.model.MoodCount;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

/** Testes de persistência do Room (RF06) e isolamento entre usuários (privacidade). */
@RunWith(AndroidJUnit4.class)
public class DatabaseTest {

    private AppDatabase db;
    private UserDao userDao;
    private DiaryDao diaryDao;
    private MoodDao moodDao;
    private SettingsDao settingsDao;

    @Before
    public void criarBanco() {
        Context ctx = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(ctx, AppDatabase.class)
                .allowMainThreadQueries()
                .build();
        userDao = db.userDao();
        diaryDao = db.diaryDao();
        moodDao = db.moodDao();
        settingsDao = db.settingsDao();
    }

    @After
    public void fecharBanco() {
        db.close();
    }

    private long novoUsuario(String email) {
        User u = new User();
        u.name = "Teste";
        u.email = email;
        u.salt = "salt";
        u.passwordHash = "hash";
        u.createdAt = System.currentTimeMillis();
        return userDao.insert(u);
    }

    private DiaryEntry entrada(long userId, String title, int mood) {
        DiaryEntry e = new DiaryEntry();
        e.userId = userId;
        e.title = title;
        e.content = "conteudo de " + title;
        e.date = System.currentTimeMillis();
        e.mood = mood;
        e.moodIntensity = 3;
        e.createdAt = e.date;
        e.updatedAt = e.date;
        return e;
    }

    @Test
    public void inserirEBuscarUsuario() {
        long id = novoUsuario("a@a.com");
        User carregado = userDao.findByEmail("a@a.com");
        assertNotNull(carregado);
        assertEquals(id, carregado.id);
    }

    @Test
    public void emailDuplicado_detectadoPorContagem() {
        novoUsuario("dup@a.com");
        assertEquals(1, userDao.countByEmail("dup@a.com"));
        assertEquals(0, userDao.countByEmail("outro@a.com"));
    }

    @Test
    public void persistenciaDeEntrada() {
        long user = novoUsuario("u@a.com");
        long entryId = diaryDao.insert(entrada(user, "Meu dia", 2));
        DiaryEntry carregada = diaryDao.findByIdForUser(entryId, user);
        assertNotNull(carregada);
        assertEquals("Meu dia", carregada.title);
    }

    @Test
    public void isolamentoEntreUsuarios() {
        long user1 = novoUsuario("u1@a.com");
        long user2 = novoUsuario("u2@a.com");
        diaryDao.insert(entrada(user1, "Entrada do 1", 1));
        diaryDao.insert(entrada(user1, "Outra do 1", 2));
        diaryDao.insert(entrada(user2, "Entrada do 2", 0));

        List<DiaryEntry> doUsuario1 = diaryDao.getAllForUserSync(user1);
        assertEquals(2, doUsuario1.size());
        for (DiaryEntry e : doUsuario1) {
            assertEquals(user1, e.userId);
        }
        assertEquals(1, diaryDao.countForUser(user2));

        // Uma entrada do user2 nunca deve ser acessível informando o user1.
        long idDoUser2 = diaryDao.getAllForUserSync(user2).get(0).id;
        assertNull(diaryDao.findByIdForUser(idDoUser2, user1));
    }

    @Test
    public void agregacaoDeHumor() {
        long user = novoUsuario("humor@a.com");
        long e1 = diaryDao.insert(entrada(user, "d1", 1));
        long e2 = diaryDao.insert(entrada(user, "d2", 1));
        long e3 = diaryDao.insert(entrada(user, "d3", 2));
        moodDao.insert(mood(user, e1, 1));
        moodDao.insert(mood(user, e2, 1));
        moodDao.insert(mood(user, e3, 2));

        long start = 0L;
        long end = Long.MAX_VALUE;
        List<MoodCount> counts = moodDao.countByMoodInRange(user, start, end);
        int totalMood1 = 0;
        for (MoodCount c : counts) {
            if (c.mood == 1) {
                totalMood1 = c.count;
            }
        }
        assertEquals(2, totalMood1);
    }

    @Test
    public void preferenciasPadrao() {
        long user = novoUsuario("pref@a.com");
        settingsDao.upsert(AppSettings.defaultsFor(user));
        AppSettings s = settingsDao.getForUser(user);
        assertNotNull(s);
        assertEquals(AppSettings.THEME_MODE_SYSTEM, s.themeMode);
        assertTrue(s.confirmDelete);
    }

    private MoodEntry mood(long userId, long diaryEntryId, int mood) {
        MoodEntry m = new MoodEntry();
        m.userId = userId;
        m.diaryEntryId = diaryEntryId;
        m.date = System.currentTimeMillis();
        m.mood = mood;
        m.intensity = 3;
        return m;
    }
}
