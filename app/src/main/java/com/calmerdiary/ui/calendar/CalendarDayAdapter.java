package com.calmerdiary.ui.calendar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.calmerdiary.R;

import java.util.ArrayList;
import java.util.List;

/** Renderiza a grade de dias do mês. Dias com entrada ganham círculo vinho. */
public class CalendarDayAdapter extends RecyclerView.Adapter<CalendarDayAdapter.DayViewHolder> {

    public interface OnDayClickListener {
        void onDayClick(long millis);
    }

    /** Célula do calendário; day == 0 representa espaço vazio antes do dia 1. */
    public static class DayCell {
        public final int day;
        public final long millis;
        public final boolean hasEntry;
        public final boolean selected;

        public DayCell(int day, long millis, boolean hasEntry, boolean selected) {
            this.day = day;
            this.millis = millis;
            this.hasEntry = hasEntry;
            this.selected = selected;
        }
    }

    private final List<DayCell> cells = new ArrayList<>();
    private final OnDayClickListener listener;

    public CalendarDayAdapter(OnDayClickListener listener) {
        this.listener = listener;
    }

    public void setCells(List<DayCell> newCells) {
        cells.clear();
        cells.addAll(newCells);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_calendar_day, parent, false);
        return new DayViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        holder.bind(cells.get(position));
    }

    @Override
    public int getItemCount() {
        return cells.size();
    }

    class DayViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvDay;

        DayViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDay = itemView.findViewById(R.id.tvDay);
        }

        void bind(DayCell cell) {
            if (cell.day == 0) {
                tvDay.setText("");
                tvDay.setBackground(null);
                tvDay.setOnClickListener(null);
                tvDay.setClickable(false);
                return;
            }
            tvDay.setText(String.valueOf(cell.day));

            if (cell.selected) {
                tvDay.setBackgroundResource(R.drawable.bg_calendar_selected);
            } else if (cell.hasEntry) {
                tvDay.setBackgroundResource(R.drawable.bg_streak_day_active);
            } else {
                tvDay.setBackground(null);
            }

            tvDay.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDayClick(cell.millis);
                }
            });
        }
    }
}
