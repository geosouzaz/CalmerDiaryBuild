package com.calmerdiary.ui.login;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.calmerdiary.data.entities.User;
import com.calmerdiary.data.repository.AuthRepository;
import com.calmerdiary.util.Callback;
import com.calmerdiary.util.Event;

public class LoginViewModel extends AndroidViewModel {

    private final AuthRepository authRepository;

    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<Event<User>> loginSuccess = new MutableLiveData<>();
    private final MutableLiveData<Event<String>> errorMessage = new MutableLiveData<>();

    public LoginViewModel(@NonNull Application application) {
        super(application);
        this.authRepository = new AuthRepository(application);
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<Event<User>> getLoginSuccess() {
        return loginSuccess;
    }

    public LiveData<Event<String>> getErrorMessage() {
        return errorMessage;
    }

    public void login(String email, String password) {
        loading.setValue(true);
        authRepository.login(email, password, new Callback<User>() {
            @Override
            public void onSuccess(User user) {
                loading.setValue(false);
                loginSuccess.setValue(new Event<>(user));
            }

            @Override
            public void onError(Exception error) {
                loading.setValue(false);
                errorMessage.setValue(new Event<>(error.getMessage()));
            }
        });
    }
}
