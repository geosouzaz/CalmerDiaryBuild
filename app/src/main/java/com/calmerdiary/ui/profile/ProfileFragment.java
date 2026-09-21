package com.calmerdiary.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.calmerdiary.R;
import com.calmerdiary.databinding.FragmentProfileBinding;
import com.calmerdiary.ui.login.LoginActivity;
import com.calmerdiary.ui.settings.SettingsActivity;
import com.calmerdiary.util.SessionManager;
import com.calmerdiary.util.ThemeManager;

/** Aba "Meu" — dados do usuário e logout (será ampliada na etapa de perfil). */
public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SessionManager session = new SessionManager(requireContext());
        binding.tvName.setText(session.getUserName());
        binding.tvEmail.setText(session.getUserEmail());

        ProfileViewModel viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        AchievementAdapter achievementAdapter = new AchievementAdapter();
        binding.rvAchievements.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvAchievements.setAdapter(achievementAdapter);

        viewModel.getStats().observe(getViewLifecycleOwner(), stats -> {
            binding.tvStatTotal.setText(String.valueOf(stats.totalEntries));
            binding.tvStatActive.setText(String.valueOf(stats.activeDays));
            binding.tvStatStreak.setText(String.valueOf(stats.longestStreak));
        });

        viewModel.getAchievements().observe(getViewLifecycleOwner(),
                achievementAdapter::setItems);

        binding.btnSettings.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), SettingsActivity.class)));

        binding.btnLogout.setOnClickListener(v -> {
            session.clear();
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish();
        });

        setupThemeSelector();
    }

    private void setupThemeSelector() {
        // Marca o botão do tema atual (antes de escutar, para não recriar na abertura).
        String mode = ThemeManager.getMode(requireContext());
        int checkedId;
        if (ThemeManager.MODE_LIGHT.equals(mode)) {
            checkedId = R.id.btnThemeLight;
        } else if (ThemeManager.MODE_DARK.equals(mode)) {
            checkedId = R.id.btnThemeDark;
        } else {
            checkedId = R.id.btnThemeSystem;
        }
        binding.toggleTheme.check(checkedId);

        binding.toggleTheme.addOnButtonCheckedListener((group, id, isChecked) -> {
            if (!isChecked) {
                return;
            }
            String newMode;
            if (id == R.id.btnThemeLight) {
                newMode = ThemeManager.MODE_LIGHT;
            } else if (id == R.id.btnThemeDark) {
                newMode = ThemeManager.MODE_DARK;
            } else {
                newMode = ThemeManager.MODE_SYSTEM;
            }
            if (!newMode.equals(ThemeManager.getMode(requireContext()))) {
                ThemeManager.setMode(requireContext(), newMode);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
