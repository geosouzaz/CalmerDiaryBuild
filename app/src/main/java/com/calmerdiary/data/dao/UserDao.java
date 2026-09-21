package com.calmerdiary.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.calmerdiary.data.entities.User;

@Dao
public interface UserDao {

    @Insert
    long insert(User user);

    @Update
    int update(User user);

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    User findByEmail(String email);

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    User findById(long id);

    @Query("SELECT COUNT(*) FROM users WHERE email = :email")
    int countByEmail(String email);

    @Query("DELETE FROM users WHERE id = :id")
    int deleteById(long id);

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    LiveData<User> observeById(long id);
}
