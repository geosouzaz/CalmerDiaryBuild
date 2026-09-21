package com.calmerdiary.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.calmerdiary.R;
import com.calmerdiary.data.entities.User;
import com.calmerdiary.databinding.ActivityLoginBinding;
import com.calmerdiary.ui.MainActivity;
import com.calmerdiary.ui.register.RegisterActivity;
import com.calmerdiary.util.BiometricHelper;
import com.calmerdiary.util.BiometricPrefs;
import com.calmerdiary.util.SessionManager;
import com.calmerdiary.util.ValidationUtils;

/** Tela de login (RF02). */
public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private LoginViewModel viewModel;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        session = new SessionManager(this);

        binding.btnLogin.setOnClickListener(v -> attemptLogin());
        binding.btnGoRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));

        setupBiometric();
        observeViewModel();
    }

    private void setupBiometric() {
        BiometricPrefs biometricPrefs = new BiometricPrefs(this);
        boolean canOffer = biometricPrefs.isEnabled()
                && biometricPrefs.getUserId() > 0
                && BiometricHelper.isAvailable(this);

        binding.btnBiometric.setVisibility(canOffer ? View.VISIBLE : View.GONE);
        if (!canOffer) {
            return;
        }
        binding.btnBiometric.setOnClickListener(v ->
                BiometricHelper.authenticate(this,
                        getString(R.string.action_login_biometric),
                        biometricPrefs.getEmail(),
                        new BiometricHelper.AuthCallback() {
                            @Override
                            public void onSuccess() {
                                User user = new User();
                                user.id = biometricPrefs.getUserId();
                                user.name = biometricPrefs.getName();
                                user.email = biometricPrefs.getEmail();
                                session.saveSession(user);
                                goToHome();
                            }

                            @Override
                            public void onError(String message) {
                                // Cancelado ou falhou — segue com login por senha.
                            }
                        }));
    }

    private void attemptLogin() {
        clearErrors();
        String email = textOf(binding.etEmail);
        String password = textOf(binding.etPassword);

        boolean valid = true;
        if (!ValidationUtils.isValidEmail(email)) {
            binding.tilEmail.setError(getString(R.string.error_email_invalid));
            valid = false;
        }
        if (ValidationUtils.isBlank(password)) {
            binding.tilPassword.setError(getString(R.string.error_required));
            valid = false;
        }
        if (!valid) {
            return;
        }
        viewModel.login(email, password);
    }

    private void observeViewModel() {
        viewModel.getLoading().observe(this, this::setLoading);

        viewModel.getLoginSuccess().observe(this, event -> {
            if (event == null) {
                return;
            }
            User user = event.getIfNotHandled();
            if (user != null) {
                session.saveSession(user);
                goToHome();
            }
        });

        viewModel.getErrorMessage().observe(this, event -> {
            if (event == null) {
                return;
            }
            String message = event.getIfNotHandled();
            if (message != null) {
                showError(message);
            }
        });
    }

    private void goToHome() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setLoading(boolean loading) {
        binding.progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnLogin.setEnabled(!loading);
        binding.btnGoRegister.setEnabled(!loading);
    }

    private void showError(String message) {
        binding.tvError.setText(TextUtils.isEmpty(message)
                ? getString(R.string.error_generic) : message);
        binding.tvError.setVisibility(View.VISIBLE);
        Toast.makeText(this, binding.tvError.getText(), Toast.LENGTH_SHORT).show();
    }

    private void clearErrors() {
        binding.tilEmail.setError(null);
        binding.tilPassword.setError(null);
        binding.tvError.setVisibility(View.GONE);
    }

    private String textOf(com.google.android.material.textfield.TextInputEditText field) {
        return field.getText() == null ? "" : field.getText().toString().trim();
    }
}
