package com.calmerdiary.ui.diary;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.calmerdiary.data.entities.DiaryEntry;
import com.calmerdiary.databinding.ItemDiaryEntryBinding;
import com.calmerdiary.model.Mood;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** Lista de entradas do diário na Home. */
public class EntryAdapter extends ListAdapter<DiaryEntry, EntryAdapter.EntryViewHolder> {

    public interface OnEntryClickListener {
        void onEntryClick(DiaryEntry entry);
    }

    private static final Locale PT_BR = new Locale("pt", "BR");

    private final OnEntryClickListener listener;

    public EntryAdapter(OnEntryClickListener listener) {
        super(DIFF);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<DiaryEntry> DIFF =
            new DiffUtil.ItemCallback<DiaryEntry>() {
                @Override
                public boolean areItemsTheSame(@NonNull DiaryEntry a, @NonNull DiaryEntry b) {
                    return a.id == b.id;
                }

                @Override
                public boolean areContentsTheSame(@NonNull DiaryEntry a, @NonNull DiaryEntry b) {
                    return a.updatedAt == b.updatedAt
                            && a.mood == b.mood
                            && equals(a.title, b.title)
                            && equals(a.imageUri, b.imageUri);
                }

                private boolean equals(String a, String b) {
                    return a == null ? b == null : a.equals(b);
                }
            };

    @NonNull
    @Override
    public EntryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemDiaryEntryBinding binding = ItemDiaryEntryBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new EntryViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull EntryViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class EntryViewHolder extends RecyclerView.ViewHolder {
        private final ItemDiaryEntryBinding binding;

        EntryViewHolder(ItemDiaryEntryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(DiaryEntry entry) {
            Date date = new Date(entry.date);
            binding.tvDay.setText(new SimpleDateFormat("dd", PT_BR).format(date));
            binding.tvMonthYear.setText(new SimpleDateFormat("MMM, yyyy", PT_BR).format(date));
            binding.tvWeekday.setText(new SimpleDateFormat("EEE", PT_BR).format(date));

            binding.tvTitle.setText(entry.title);
            binding.tvSnippet.setText(entry.content);

            String emoji = Mood.emojiOf(entry.mood);
            if (emoji.isEmpty()) {
                binding.tvMood.setVisibility(View.GONE);
            } else {
                binding.tvMood.setVisibility(View.VISIBLE);
                binding.tvMood.setText(emoji);
            }

            if (entry.imageUri != null && !entry.imageUri.isEmpty()) {
                binding.ivThumb.setVisibility(View.VISIBLE);
                try {
                    binding.ivThumb.setImageURI(Uri.parse(entry.imageUri));
                } catch (Exception e) {
                    binding.ivThumb.setVisibility(View.GONE);
                }
            } else {
                binding.ivThumb.setVisibility(View.GONE);
            }

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEntryClick(entry);
                }
            });
        }
    }
}
