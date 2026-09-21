package com.calmerdiary.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.calmerdiary.data.entities.DiaryEntry;

import java.util.List;

@Dao
public interface DiaryDao {

    @Insert
    long insert(DiaryEntry entry);

    @Update
    int update(DiaryEntry entry);

    @Delete
    int delete(DiaryEntry entry);

    // --- Consulta única (sempre validando o dono) ---
    @Query("SELECT * FROM diary_entries WHERE id = :id AND userId = :userId LIMIT 1")
    DiaryEntry findByIdForUser(long id, long userId);

    @Query("SELECT * FROM diary_entries WHERE id = :id AND userId = :userId LIMIT 1")
    LiveData<DiaryEntry> observeByIdForUser(long id, long userId);

    // --- Listagens observáveis ---
    @Query("SELECT * FROM diary_entries WHERE userId = :userId ORDER BY date DESC, createdAt DESC")
    LiveData<List<DiaryEntry>> observeAllForUser(long userId);

    @Query("SELECT * FROM diary_entries WHERE userId = :userId "
            + "AND (title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%') "
            + "ORDER BY date DESC, createdAt DESC")
    LiveData<List<DiaryEntry>> search(long userId, String query);

    @Query("SELECT * FROM diary_entries WHERE userId = :userId AND date BETWEEN :start AND :end "
            + "ORDER BY date DESC, createdAt DESC")
    LiveData<List<DiaryEntry>> observeInRange(long userId, long start, long end);

    /** Busca (título/conteúdo) combinada com filtro de período. Query vazia casa tudo. */
    @Query("SELECT * FROM diary_entries WHERE userId = :userId "
            + "AND (title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%') "
            + "AND date BETWEEN :start AND :end "
            + "ORDER BY date DESC, createdAt DESC")
    LiveData<List<DiaryEntry>> searchInRange(long userId, String query, long start, long end);

    // --- Contagens ---
    @Query("SELECT COUNT(*) FROM diary_entries WHERE userId = :userId")
    LiveData<Integer> observeCountForUser(long userId);

    @Query("SELECT COUNT(*) FROM diary_entries WHERE userId = :userId")
    int countForUser(long userId);

    @Query("SELECT COUNT(*) FROM diary_entries WHERE userId = :userId AND date BETWEEN :start AND :end")
    int countInRange(long userId, long start, long end);

    // --- Datas (calendário e cálculo de sequência) ---
    @Query("SELECT DISTINCT date FROM diary_entries WHERE userId = :userId ORDER BY date")
    List<Long> getEntryDates(long userId);

    @Query("SELECT DISTINCT date FROM diary_entries WHERE userId = :userId ORDER BY date")
    LiveData<List<Long>> observeEntryDates(long userId);

    // --- Versões síncronas (uso em background/testes) ---
    @Query("SELECT * FROM diary_entries WHERE userId = :userId AND date BETWEEN :start AND :end "
            + "ORDER BY date DESC, createdAt DESC")
    List<DiaryEntry> getInRangeSync(long userId, long start, long end);

    @Query("SELECT * FROM diary_entries WHERE userId = :userId ORDER BY date DESC, createdAt DESC")
    List<DiaryEntry> getAllForUserSync(long userId);
}
