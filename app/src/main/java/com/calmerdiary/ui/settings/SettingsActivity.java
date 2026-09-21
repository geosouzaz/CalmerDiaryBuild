package com.calmerdiary.ui.settings;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.calmerdiary.R;
import com.calmerdiary.data.entities.AppSettings;
import com.calmerdiary.data.entities.User;
import com.calmerdiary.data.repository.AuthRepository;
import com.calmerdiary.data.repository.SettingsRepository;
import com.calmerdiary.databinding.ActivitySettingsBinding;
import com.calmerdiary.databinding.DialogChangePasswordBinding;
import com.calmerdiary.ui.login.LoginActivity;
import com.calmerdiary.util.BiometricHelper;
import com.calmerdiary.util.BiometricPrefs;
import com.calmerdiary.util.Callback;
import com.calmerdiary.util.SessionManager;
import com.calmerdiary.util.ThemeManager;
import com.calmerdiary.util.ValidationUtils;
import com.calmerdiary.util.WindowInsetsUtil;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

/** Tela de configurações: aparência, segurança, diário e conta. */
public class SettingsActivity extends AppCompatActivity {

    private ActivitySettingsBinding binding;
    private SessionManager session;
    private SettingsRepository settingsRepository;
    private AuthRepository authRepository;
    private BiometricPrefs biometricPrefs;

    private AppSettings currentSettings;
    private long userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        session = new SessionManager(this);
        settingsRepository = new SettingsRepository(this);
        authRepository = new AuthRepository(this);
        biometricPrefs = new BiometricPrefs(this);
        userId = session.getUserId();

        WindowInsetsUtil.applyTopInset(binding.toolbar);
        WindowInsetsUtil.applyBottomInset(binding.getRoot());
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        setupTheme();
        setupBiometricAvailability();

        binding.rowChangePassword.setOnClickListener(v -> showChangePasswordDialog());
        binding.rowLogout.setOnClickListener(v -> logout());
        binding.rowDeleteAccount.setOnClickListener(v -> confirmDeleteAccount());

        loadSettings();
    }

    private void setupTheme() {
        String mode = ThemeManager.getMode(this);
        int checkedId;
        if (ThemeManager.MODE_LIGHT.equals(mode)) {
            checkedId = R.id.btnThemeLight;
        } else if (ThemeManager.MODE_DARK.equals(mode)) {
            checkedId = R.id.btnThemeDark;
        } else {
            checkedId = R.id.btnThemeSystem;
        }
        binding.themeGroup.check(checkedId);
        binding.themeGroup.addOnButtonCheckedListener((group, id, isChecked) -> {
            if (!isChecked) {
                return;
            }
            String newMode = id == R.id.btnThemeLight ? ThemeManager.MODE_LIGHT
                    : id == R.id.btnThemeDark ? ThemeManager.MODE_DARK
                    : ThemeManager.MODE_SYSTEM;
            if (!newMode.equals(ThemeManager.getMode(this))) {
                ThemeManager.setMode(this, newMode);
            }
        });
    }

    private void setupBiometricAvailability() {
        boolean available = BiometricHelper.isAvailable(this);
        binding.switchBiometric.setEnabled(available);
        binding.tvBiometricHint.setVisibility(available ? View.GONE : View.VISIBLE);
    }

    private void loadSettings() {
        settingsRepository.getSettings(userId, new Callback<AppSettings>() {
            @Override
            public void onSuccess(AppSettings settings) {
                currentSettings = settings;
                bindSettings();
            }

            @Override
            public void onError(Exception e) {
                currentSettings = AppSettings.defaultsFor(userId);
                bindSettings();
            }
        });
    }

    private void bindSettings() {
        // Marca os valores antes de registrar os listeners (evita disparo inicial).
        binding.switchConfirmDelete.setOnCheckedChangeListener(null);
        binding.switchBiometric.setOnCheckedChangeListener(null);

        binding.switchConfirmDelete.setChecked(currentSettings.confirmDelete);
        boolean biometricOn = currentSettings.biometricEnabled
                && BiometricHelper.isAvailable(this);
        binding.switchBiometric.setChecked(biometricOn);

        binding.switchConfirmDelete.setOnCheckedChangeListener((v, checked) -> {
            currentSettings.confirmDelete = checked;
            settingsRepository.save(currentSettings, null);
        });

        binding.switchBiometric.setOnCheckedChangeListener((v, checked) -> onBiometricToggled(checked));
    }

    private void onBiometricToggled(boolean checked) {
        currentSettings.biometricEnabled = checked;
        settingsRepository.save(currentSettings, null);
        if (checked) {
            User user = new User();
            user.id = userId;
            user.name = session.getUserName();
            user.email = session.getUserEmail();
            biometricPrefs.enableFor(user);
            Toast.makeText(this, R.string.biometric_enabled_msg, Toast.LENGTH_SHORT).show();
        } else {
            biometricPrefs.disable();
        }
    }

    private void showChangePasswordDialog() {
        DialogChangePasswordBinding dialogBinding =
                DialogChangePasswordBinding.inflate(LayoutInflater.from(this));
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.change_password_title)
                .setView(dialogBinding.getRoot())
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_save, (dialog, which) -> {
                    String current = text(dialogBinding.etCurrent);
                    String novo = text(dialogBinding.etNew);
                    if (!ValidationUtils.isValidPassword(novo)) {
                        Toast.makeText(this, R.string.error_password_short, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    authRepository.changePassword(userId, current, novo, new Callback<Boolean>() {
                        @Override
                        public void onSuccess(Boolean ok) {
                            Toast.makeText(SettingsActivity.this,
                                    R.string.success_password_changed, Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(Exception e) {
                            Toast.makeText(SettingsActivity.this,
                                    e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .show();
    }

    private void confirmDeleteAccount() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete_account_title)
                .setMessage(R.string.delete_account_message)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.setting_delete_account, (dialog, which) ->
                        authRepository.deleteAccount(userId, new Callback<Boolean>() {
                            @Override
                            public void onSuccess(Boolean ok) {
                                session.clear();
                                biometricPrefs.disable();
                                Toast.makeText(SettingsActivity.this,
                                        R.string.success_account_deleted, Toast.LENGTH_SHORT).show();
                                goToLogin();
                            }

                            @Override
                            public void onError(Exception e) {
                                Toast.makeText(SettingsActivity.this,
                                        R.string.error_generic, Toast.LENGTH_SHORT).show();
                            }
                        }))
                .show();
    }

    private void logout() {
        session.clear();
        goToLogin();
    }

    private void goToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private String text(TextInputEditText field) {
        return field.getText() == null ? "" : field.getText().toString();
    }
}
