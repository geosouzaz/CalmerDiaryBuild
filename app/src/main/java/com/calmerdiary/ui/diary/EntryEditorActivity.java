package com.calmerdiary.ui.diary;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.calmerdiary.R;
import com.calmerdiary.data.entities.DiaryEntry;
import com.calmerdiary.databinding.ActivityEntryEditorBinding;
import com.calmerdiary.model.Mood;
import com.calmerdiary.util.DateUtils;
import com.calmerdiary.util.WindowInsetsUtil;

import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** Tela de criar/editar uma entrada do diário (RF03). */
public class EntryEditorActivity extends AppCompatActivity {

    public static final String EXTRA_ENTRY_ID = "extra_entry_id";

    private ActivityEntryEditorBinding binding;
    private EntryEditorViewModel viewModel;

    private final List<View> moodViews = new ArrayList<>();
    private int selectedMoodIndex = -1;
    private long selectedDate = DateUtils.startOfDay(System.currentTimeMillis());
    private String imageUri = null;
    private long entryId = 0L;

    private final ActivityResultLauncher<String[]> pickImage =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri != null) {
                    try {
                        getContentResolver().takePersistableUriPermission(
                                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    } catch (SecurityException ignored) {
                        // Alguns provedores não concedem permissão persistente.
                    }
                    imageUri = uri.toString();
                    showImagePreview();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEntryEditorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(EntryEditorViewModel.class);
        entryId = getIntent().getLongExtra(EXTRA_ENTRY_ID, 0L);

        WindowInsetsUtil.applyTopInset(binding.toolbar);
        WindowInsetsUtil.applyBottomInset(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.toolbar.setTitle(entryId > 0 ? R.string.editor_title_edit : R.string.editor_title_new);

        buildMoodSelector();
        updateDateButton();

        binding.btnDate.setOnClickListener(v -> openDatePicker());
        binding.btnAddImage.setOnClickListener(v -> pickImage.launch(new String[]{"image/*"}));
        binding.btnRemoveImage.setOnClickListener(v -> {
            imageUri = null;
            binding.imagePreviewContainer.setVisibility(View.GONE);
        });
        binding.btnSave.setOnClickListener(v -> save());

        observeViewModel();

        if (entryId > 0) {
            viewModel.loadEntry(entryId);
        }
    }

    private void buildMoodSelector() {
        LayoutInflater inflater = getLayoutInflater();
        for (Mood mood : Mood.all()) {
            View item = inflater.inflate(R.layout.item_mood, binding.moodContainer, false);
            ((TextView) item.findViewById(R.id.tvEmoji)).setText(mood.emoji);
            ((TextView) item.findViewById(R.id.tvMoodLabel)).setText(getString(mood.labelRes));
            final int index = mood.index;
            item.setOnClickListener(v -> selectMood(index));
            moodViews.add(item);
            binding.moodContainer.addView(item);
        }
    }

    private void selectMood(int index) {
        selectedMoodIndex = index;
        int selectedColor = getColor(R.color.white);
        int mutedColor = resolveThemeColor(com.google.android.material.R.attr.colorOnSurfaceVariant);
        for (int i = 0; i < moodViews.size(); i++) {
            View v = moodViews.get(i);
            boolean isSelected = i == index;
            v.setSelected(isSelected);
            ((TextView) v.findViewById(R.id.tvMoodLabel))
                    .setTextColor(isSelected ? selectedColor : mutedColor);
        }
    }

    private void openDatePicker() {
        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(R.string.label_date)
                .setSelection(selectedDate)
                .build();
        picker.addOnPositiveButtonClickListener(selection -> {
            selectedDate = DateUtils.startOfDay(selection);
            updateDateButton();
        });
        picker.show(getSupportFragmentManager(), "date_picker");
    }

    private void updateDateButton() {
        SimpleDateFormat format = new SimpleDateFormat("dd 'de' MMM 'de' yyyy", new Locale("pt", "BR"));
        binding.btnDate.setText(format.format(new Date(selectedDate)));
    }

    private void showImagePreview() {
        if (imageUri == null) {
            binding.imagePreviewContainer.setVisibility(View.GONE);
            return;
        }
        binding.imagePreviewContainer.setVisibility(View.VISIBLE);
        binding.ivPreview.setImageURI(Uri.parse(imageUri));
    }

    private void save() {
        binding.tilTitle.setError(null);
        binding.tilContent.setError(null);

        String title = text(binding.etTitle);
        String content = text(binding.etContent);

        if (title.isEmpty()) {
            binding.tilTitle.setError(getString(R.string.error_title_required));
            return;
        }
        if (content.isEmpty()) {
            binding.tilContent.setError(getString(R.string.error_content_required));
            return;
        }

        DiaryEntry entry = new DiaryEntry();
        entry.id = entryId;
        entry.userId = viewModel.getUserId();
        entry.title = title;
        entry.content = content;
        entry.date = selectedDate;
        entry.mood = selectedMoodIndex;
        entry.moodIntensity = selectedMoodIndex >= 0 ? 3 : 0;
        entry.imageUri = imageUri;

        viewModel.save(entry);
    }

    private void observeViewModel() {
        viewModel.getLoading().observe(this, loading ->
                binding.btnSave.setEnabled(!loading));

        viewModel.getSaved().observe(this, event -> {
            if (event == null) {
                return;
            }
            Long id = event.getIfNotHandled();
            if (id != null) {
                Toast.makeText(this, R.string.success_entry_saved, Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            }
        });

        viewModel.getError().observe(this, event -> {
            if (event == null) {
                return;
            }
            String message = event.getIfNotHandled();
            if (message != null) {
                Toast.makeText(this, R.string.error_generic, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getLoadedEntry().observe(this, this::populate);
    }

    private void populate(DiaryEntry entry) {
        if (entry == null) {
            return;
        }
        binding.etTitle.setText(entry.title);
        binding.etContent.setText(entry.content);
        selectedDate = entry.date;
        updateDateButton();
        if (entry.mood >= 0) {
            selectMood(entry.mood);
        }
        if (entry.imageUri != null) {
            imageUri = entry.imageUri;
            showImagePreview();
        }
    }

    private int resolveThemeColor(int attr) {
        android.util.TypedValue tv = new android.util.TypedValue();
        getTheme().resolveAttribute(attr, tv, true);
        return tv.data;
    }

    private String text(com.google.android.material.textfield.TextInputEditText field) {
        return field.getText() == null ? "" : field.getText().toString().trim();
    }
}
