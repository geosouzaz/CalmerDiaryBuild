package com.calmerdiary.ui.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.calmerdiary.databinding.ActivitySplashBinding;
import com.calmerdiary.ui.MainActivity;
import com.calmerdiary.ui.login.LoginActivity;
import com.calmerdiary.util.SessionManager;

/**
 * Tela inicial. Exibe a identidade do app por um breve momento e então
 * direciona para a Home (se já houver sessão) ou para o Login.
 */
public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY_MS = 1100L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivitySplashBinding binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SessionManager session = new SessionManager(this);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = session.isLoggedIn()
                    ? new Intent(this, MainActivity.class)
                    : new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }, SPLASH_DELAY_MS);
    }
}
