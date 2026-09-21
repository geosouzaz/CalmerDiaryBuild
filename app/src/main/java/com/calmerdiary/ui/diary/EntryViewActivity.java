package com.calmerdiary.ui.diary;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.calmerdiary.R;
import com.calmerdiary.data.entities.DiaryEntry;
import com.calmerdiary.databinding.ActivityEntryViewBinding;
import com.calmerdiary.model.Mood;
import com.calmerdiary.util.WindowInsetsUtil;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** Tela de leitura de uma entrada, com opções de editar e excluir. */
public class EntryViewActivity extends AppCompatActivity {

    public static final String EXTRA_ENTRY_ID = "extra_entry_id";
    private static final Locale PT_BR = new Locale("pt", "BR");

    private ActivityEntryViewBinding binding;
    private EntryViewViewModel viewModel;
    private long entryId;
    private DiaryEntry currentEntry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEntryViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(EntryViewViewModel.class);
        entryId = getIntent().getLongExtra(EXTRA_ENTRY_ID, 0L);

        WindowInsetsUtil.applyTopInset(binding.toolbar);
        WindowInsetsUtil.applyBottomInset(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.btnEdit.setOnClickListener(v -> openEditor());
        binding.btnDelete.setOnClickListener(v -> confirmDelete());

        viewModel.observe(entryId).observe(this, this::render);

        viewModel.getDeleted().observe(this, event -> {
            if (event == null) {
                return;
            }
            Boolean ok = event.getIfNotHandled();
            if (ok != null && ok) {
                Toast.makeText(this, R.string.success_entry_deleted, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void render(DiaryEntry entry) {
        if (entry == null) {
            // Entrada não existe mais (ex.: excluída) — encerra a tela.
            finish();
            return;
        }
        currentEntry = entry;

        binding.tvTitle.setText(entry.title);

        SimpleDateFormat format = new SimpleDateFormat("EEEE, dd 'de' MMMM 'de' yyyy", PT_BR);
        binding.tvDate.setText(format.format(new Date(entry.date)));

        Mood mood = Mood.fromIndex(entry.mood);
        if (mood == null) {
            binding.tvMood.setVisibility(View.GONE);
        } else {
            binding.tvMood.setVisibility(View.VISIBLE);
            binding.tvMood.setText(mood.emoji + "  " + getString(mood.labelRes));
        }

        binding.tvContent.setText(entry.content);

        if (entry.imageUri != null && !entry.imageUri.isEmpty()) {
            binding.ivImage.setVisibility(View.VISIBLE);
            try {
                binding.ivImage.setImageURI(Uri.parse(entry.imageUri));
            } catch (Exception e) {
                binding.ivImage.setVisibility(View.GONE);
            }
        } else {
            binding.ivImage.setVisibility(View.GONE);
        }
    }

    private void openEditor() {
        Intent intent = new Intent(this, EntryEditorActivity.class);
        intent.putExtra(EntryEditorActivity.EXTRA_ENTRY_ID, entryId);
        startActivity(intent);
    }

    private void confirmDelete() {
        if (currentEntry == null) {
            return;
        }
        viewModel.isConfirmDeleteEnabled(new com.calmerdiary.util.Callback<Boolean>() {
            @Override
            public void onSuccess(Boolean confirm) {
                if (confirm) {
                    showDeleteDialog();
                } else {
                    viewModel.delete(currentEntry);
                }
            }

            @Override
            public void onError(Exception e) {
                showDeleteDialog();
            }
        });
    }

    private void showDeleteDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete_confirm_title)
                .setMessage(R.string.delete_confirm_message)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_delete, (dialog, which) -> viewModel.delete(currentEntry))
                .show();
    }
}
