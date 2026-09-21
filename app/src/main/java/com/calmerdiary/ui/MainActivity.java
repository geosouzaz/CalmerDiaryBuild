package com.calmerdiary.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.calmerdiary.R;
import com.calmerdiary.databinding.ActivityMainBinding;
import com.calmerdiary.ui.calendar.CalendarFragment;
import com.calmerdiary.ui.diary.EntryEditorActivity;
import com.calmerdiary.ui.home.HomeFragment;
import com.calmerdiary.ui.login.LoginActivity;
import com.calmerdiary.ui.profile.ProfileFragment;
import com.calmerdiary.ui.stats.StatsFragment;
import com.calmerdiary.util.SessionManager;

/**
 * Host do app autenticado: navegação inferior (2 abas + FAB central "+" + 2 abas).
 */
public class MainActivity extends AppCompatActivity {

    private static final String STATE_SELECTED = "state_selected_tab";

    private ActivityMainBinding binding;
    private int selectedId = R.id.navDiary;

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED, selectedId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SessionManager session = new SessionManager(this);
        if (!session.isLoggedIn()) {
            goToLogin();
            return;
        }

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (savedInstanceState == null) {
            showFragment(new HomeFragment());
        } else {
            // Restaura a aba ativa (após rotação/troca de tema) — o
            // FragmentManager já restaura o fragment correspondente.
            selectedId = savedInstanceState.getInt(STATE_SELECTED, R.id.navDiary);
        }

        binding.navDiary.setOnClickListener(v -> select(R.id.navDiary));
        binding.navCalendar.setOnClickListener(v -> select(R.id.navCalendar));
        binding.navStats.setOnClickListener(v -> select(R.id.navStats));
        binding.navProfile.setOnClickListener(v -> select(R.id.navProfile));

        binding.fabAdd.setOnClickListener(v ->
                startActivity(new Intent(this, EntryEditorActivity.class)));

        updateSelectedTint();
    }

    private void select(int id) {
        if (id == selectedId) {
            return;
        }
        selectedId = id;
        Fragment fragment;
        if (id == R.id.navCalendar) {
            fragment = new CalendarFragment();
        } else if (id == R.id.navStats) {
            fragment = new StatsFragment();
        } else if (id == R.id.navProfile) {
            fragment = new ProfileFragment();
        } else {
            fragment = new HomeFragment();
        }
        showFragment(fragment);
        updateSelectedTint();
    }

    private void showFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.navHostContainer, fragment)
                .commit();
    }

    /** Realça (cor vinho) o ícone da aba ativa. */
    private void updateSelectedTint() {
        int selected = getColor(R.color.wine_primary);
        int muted = resolveThemeColor(com.google.android.material.R.attr.colorOnSurfaceVariant);

        ImageButton[] buttons = {
                binding.navDiary, binding.navCalendar, binding.navStats, binding.navProfile
        };
        for (ImageButton button : buttons) {
            button.setColorFilter(button.getId() == selectedId ? selected : muted);
        }
    }

    private int resolveThemeColor(int attr) {
        TypedValue tv = new TypedValue();
        getTheme().resolveAttribute(attr, tv, true);
        return tv.data;
    }

    private void goToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
