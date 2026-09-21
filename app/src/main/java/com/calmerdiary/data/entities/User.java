package com.calmerdiary.data.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Usuário do aplicativo. A senha nunca é guardada em texto puro —
 * apenas o hash PBKDF2 e o salt correspondente.
 */
@Entity(
        tableName = "users",
        indices = {@Index(value = "email", unique = true)}
)
public class User {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String name = "";

    @NonNull
    public String email = "";

    @NonNull
    public String passwordHash = "";

    @NonNull
    public String salt = "";

    public long createdAt;
}
