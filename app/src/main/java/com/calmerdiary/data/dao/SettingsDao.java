package com.calmerdiary.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.calmerdiary.data.entities.AppSettings;

@Dao
public interface SettingsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(AppSettings settings);

    @Update
    int update(AppSettings settings);

    @Query("SELECT * FROM app_settings WHERE userId = :userId LIMIT 1")
    AppSettings getForUser(long userId);

    @Query("SELECT * FROM app_settings WHERE userId = :userId LIMIT 1")
    LiveData<AppSettings> observeForUser(long userId);
}
