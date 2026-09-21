package com.calmerdiary.ui.profile;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.calmerdiary.R;
import com.calmerdiary.databinding.ItemAchievementBinding;
import com.calmerdiary.model.AchievementProgress;

import java.util.ArrayList;
import java.util.List;

/** Lista de conquistas com progresso. */
public class AchievementAdapter extends RecyclerView.Adapter<AchievementAdapter.AchViewHolder> {

    private final List<AchievementProgress> items = new ArrayList<>();

    public void setItems(List<AchievementProgress> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAchievementBinding binding = ItemAchievementBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new AchViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AchViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class AchViewHolder extends RecyclerView.ViewHolder {
        private final ItemAchievementBinding binding;

        AchViewHolder(ItemAchievementBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(AchievementProgress item) {
            binding.tvAchEmoji.setText(item.achievement.emoji);
            binding.tvAchTitle.setText(binding.getRoot().getContext().getString(item.achievement.titleRes));
            binding.tvAchDesc.setText(binding.getRoot().getContext().getString(item.achievement.descRes));
            binding.progressAch.setProgress(item.percent);
            binding.tvAchPercent.setText(
                    binding.getRoot().getContext().getString(R.string.achievement_progress, item.percent));

            // Conquistas bloqueadas ficam levemente esmaecidas.
            float alpha = item.unlocked ? 1f : 0.6f;
            binding.tvAchEmoji.setAlpha(alpha);
        }
    }
}
