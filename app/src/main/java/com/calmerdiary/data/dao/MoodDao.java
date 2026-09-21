package com.calmerdiary.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.calmerdiary.data.entities.MoodEntry;
import com.calmerdiary.model.MoodCount;

import java.util.List;

@Dao
public interface MoodDao {

    @Insert
    long insert(MoodEntry entry);

    @Query("DELETE FROM mood_entries WHERE diaryEntryId = :diaryEntryId")
    int deleteByDiaryEntry(long diaryEntryId);

    @Query("SELECT * FROM mood_entries WHERE userId = :userId AND date BETWEEN :start AND :end ORDER BY date")
    List<MoodEntry> getInRangeSync(long userId, long start, long end);

    @Query("SELECT * FROM mood_entries WHERE userId = :userId AND date BETWEEN :start AND :end ORDER BY date")
    LiveData<List<MoodEntry>> observeInRange(long userId, long start, long end);

    @Query("SELECT mood AS mood, COUNT(*) AS count FROM mood_entries "
            + "WHERE userId = :userId AND date BETWEEN :start AND :end GROUP BY mood")
    List<MoodCount> countByMoodInRange(long userId, long start, long end);
}
