package com.calmerdiary.ui.register;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.calmerdiary.R;
import com.calmerdiary.data.entities.User;
import com.calmerdiary.databinding.ActivityRegisterBinding;
import com.calmerdiary.ui.MainActivity;
import com.calmerdiary.util.SessionManager;
import com.calmerdiary.util.ValidationUtils;

/** Tela de cadastro (RF01). */
public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private RegisterViewModel viewModel;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(RegisterViewModel.class);
        session = new SessionManager(this);

        binding.btnRegister.setOnClickListener(v -> attemptRegister());
        binding.btnGoLogin.setOnClickListener(v -> finish());

        observeViewModel();
    }

    private void attemptRegister() {
        clearErrors();
        String name = textOf(binding.etName);
        String email = textOf(binding.etEmail);
        String password = passwordOf(binding.etPassword);
        String confirm = passwordOf(binding.etPasswordConfirm);

        boolean valid = true;
        if (!ValidationUtils.isValidName(name)) {
            binding.tilName.setError(getString(R.string.error_name_short));
            valid = false;
        }
        if (!ValidationUtils.isValidEmail(email)) {
            binding.tilEmail.setError(getString(R.string.error_email_invalid));
            valid = false;
        }
        if (!ValidationUtils.isValidPassword(password)) {
            binding.tilPassword.setError(getString(R.string.error_password_short));
            valid = false;
        }
        if (!ValidationUtils.passwordsMatch(password, confirm)) {
            binding.tilPasswordConfirm.setError(getString(R.string.error_password_mismatch));
            valid = false;
        }
        if (!valid) {
            return;
        }
        viewModel.register(name, email, password);
    }

    private void observeViewModel() {
        viewModel.getLoading().observe(this, this::setLoading);

        viewModel.getRegisterSuccess().observe(this, event -> {
            if (event == null) {
                return;
            }
            User user = event.getIfNotHandled();
            if (user != null) {
                session.saveSession(user);
                Toast.makeText(this, R.string.success_register, Toast.LENGTH_SHORT).show();
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
        binding.btnRegister.setEnabled(!loading);
        binding.btnGoLogin.setEnabled(!loading);
    }

    private void showError(String message) {
        binding.tvError.setText(TextUtils.isEmpty(message)
                ? getString(R.string.error_generic) : message);
        binding.tvError.setVisibility(View.VISIBLE);
        Toast.makeText(this, binding.tvError.getText(), Toast.LENGTH_SHORT).show();
    }

    private void clearErrors() {
        binding.tilName.setError(null);
        binding.tilEmail.setError(null);
        binding.tilPassword.setError(null);
        binding.tilPasswordConfirm.setError(null);
        binding.tvError.setVisibility(View.GONE);
    }

    private String textOf(com.google.android.material.textfield.TextInputEditText field) {
        return field.getText() == null ? "" : field.getText().toString().trim();
    }

    private String passwordOf(com.google.android.material.textfield.TextInputEditText field) {
        return field.getText() == null ? "" : field.getText().toString();
    }
}
