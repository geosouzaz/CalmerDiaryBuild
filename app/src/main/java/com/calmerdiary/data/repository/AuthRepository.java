package com.calmerdiary.data.repository;

import android.content.Context;

import com.calmerdiary.data.database.AppDatabase;
import com.calmerdiary.data.dao.SettingsDao;
import com.calmerdiary.data.dao.UserDao;
import com.calmerdiary.data.entities.AppSettings;
import com.calmerdiary.data.entities.User;
import com.calmerdiary.util.AppExecutors;
import com.calmerdiary.util.Callback;
import com.calmerdiary.util.SecurityUtils;

/**
 * Regras de cadastro e autenticação.
 * Todas as operações rodam fora da thread principal e retornam nela.
 */
public class AuthRepository {

    private final UserDao userDao;
    private final SettingsDao settingsDao;
    private final AppExecutors executors;

    public AuthRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.userDao = db.userDao();
        this.settingsDao = db.settingsDao();
        this.executors = AppExecutors.getInstance();
    }

    /** Cria uma conta. Falha se o e-mail já existir. */
    public void register(String name, String email, String password, Callback<User> callback) {
        executors.diskIO().execute(() -> {
            try {
                String normalizedEmail = email.trim().toLowerCase();
                if (userDao.countByEmail(normalizedEmail) > 0) {
                    postError(callback, new IllegalStateException("E-mail já cadastrado"));
                    return;
                }
                String salt = SecurityUtils.generateSalt();
                User user = new User();
                user.name = name.trim();
                user.email = normalizedEmail;
                user.salt = salt;
                user.passwordHash = SecurityUtils.hashPassword(password, salt);
                user.createdAt = System.currentTimeMillis();

                long id = userDao.insert(user);
                user.id = id;

                // Preferências padrão para o novo usuário.
                settingsDao.upsert(AppSettings.defaultsFor(id));

                postSuccess(callback, user);
            } catch (Exception e) {
                postError(callback, e);
            }
        });
    }

    /** Autentica por e-mail e senha. */
    public void login(String email, String password, Callback<User> callback) {
        executors.diskIO().execute(() -> {
            try {
                String normalizedEmail = email.trim().toLowerCase();
                User user = userDao.findByEmail(normalizedEmail);
                if (user == null
                        || !SecurityUtils.verifyPassword(password, user.salt, user.passwordHash)) {
                    postError(callback, new IllegalStateException("E-mail ou senha inválidos"));
                    return;
                }
                postSuccess(callback, user);
            } catch (Exception e) {
                postError(callback, e);
            }
        });
    }

    public void isEmailTaken(String email, Callback<Boolean> callback) {
        executors.diskIO().execute(() -> {
            try {
                boolean taken = userDao.countByEmail(email.trim().toLowerCase()) > 0;
                postSuccess(callback, taken);
            } catch (Exception e) {
                postError(callback, e);
            }
        });
    }

    /** Altera a senha após validar a senha atual. */
    public void changePassword(long userId, String currentPassword, String newPassword,
                               Callback<Boolean> callback) {
        executors.diskIO().execute(() -> {
            try {
                User user = userDao.findById(userId);
                if (user == null
                        || !SecurityUtils.verifyPassword(currentPassword, user.salt, user.passwordHash)) {
                    postError(callback, new IllegalStateException("Senha atual incorreta"));
                    return;
                }
                String salt = SecurityUtils.generateSalt();
                user.salt = salt;
                user.passwordHash = SecurityUtils.hashPassword(newPassword, salt);
                userDao.update(user);
                postSuccess(callback, true);
            } catch (Exception e) {
                postError(callback, e);
            }
        });
    }

    /** Exclui a conta e, por CASCADE, todas as entradas/humores do usuário. */
    public void deleteAccount(long userId, Callback<Boolean> callback) {
        executors.diskIO().execute(() -> {
            try {
                userDao.deleteById(userId);
                postSuccess(callback, true);
            } catch (Exception e) {
                postError(callback, e);
            }
        });
    }

    public void getUser(long userId, Callback<User> callback) {
        executors.diskIO().execute(() -> {
            try {
                postSuccess(callback, userDao.findById(userId));
            } catch (Exception e) {
                postError(callback, e);
            }
        });
    }

    private <T> void postSuccess(Callback<T> callback, T result) {
        if (callback != null) {
            executors.mainThread().execute(() -> callback.onSuccess(result));
        }
    }

    private <T> void postError(Callback<T> callback, Exception error) {
        if (callback != null) {
            executors.mainThread().execute(() -> callback.onError(error));
        }
    }
}
