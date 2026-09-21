package com.calmerdiary.ui.stats;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Gráfico de barras simples desenhado no Canvas (sem bibliotecas externas).
 * Cada barra tem um rótulo (texto/emoji) e um valor; a altura é proporcional
 * ao valor máximo informado.
 */
public class BarChartView extends View {

    public static class Bar {
        public final String label;
        public final float value;
        public final int color;

        public Bar(String label, float value, int color) {
            this.label = label;
            this.value = value;
            this.color = color;
        }
    }

    private final List<Bar> bars = new ArrayList<>();
    private float maxValue = 1f;
    private boolean showPercent = false;

    private final Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private int textColor = Color.WHITE;
    private int mutedColor = Color.LTGRAY;

    public BarChartView(Context context) {
        super(context);
        init();
    }

    public BarChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(sp(13));
        textPaint.setFakeBoldText(true);
        labelPaint.setTextAlign(Paint.Align.CENTER);
        labelPaint.setTextSize(sp(15));
    }

    public void setColors(int textColor, int mutedColor) {
        this.textColor = textColor;
        this.mutedColor = mutedColor;
        invalidate();
    }

    public void setData(List<Bar> data, float maxValue, boolean showPercent) {
        bars.clear();
        bars.addAll(data);
        this.maxValue = Math.max(maxValue, 1f);
        this.showPercent = showPercent;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (bars.isEmpty()) {
            return;
        }

        float valueTextH = sp(16);
        float labelTextH = sp(22);
        float topPad = valueTextH + dp(4);
        float bottomPad = labelTextH + dp(6);

        float chartHeight = getHeight() - topPad - bottomPad;
        float slot = (float) getWidth() / bars.size();
        float barWidth = Math.min(slot * 0.5f, dp(28));
        float radius = barWidth / 2f;

        textPaint.setColor(textColor);
        labelPaint.setColor(mutedColor);

        for (int i = 0; i < bars.size(); i++) {
            Bar bar = bars.get(i);
            float cx = slot * i + slot / 2f;

            float ratio = Math.max(0f, Math.min(1f, bar.value / maxValue));
            float barH = chartHeight * ratio;
            float top = topPad + (chartHeight - barH);
            float bottom = topPad + chartHeight;

            // trilho de fundo
            barPaint.setColor(withAlpha(mutedColor, 40));
            RectF track = new RectF(cx - barWidth / 2f, topPad, cx + barWidth / 2f, bottom);
            canvas.drawRoundRect(track, radius, radius, barPaint);

            // barra
            if (barH > 0) {
                barPaint.setColor(bar.color);
                RectF rect = new RectF(cx - barWidth / 2f, top, cx + barWidth / 2f, bottom);
                canvas.drawRoundRect(rect, radius, radius, barPaint);
            }

            // valor acima
            String valueText = showPercent
                    ? Math.round(bar.value) + "%"
                    : String.valueOf(Math.round(bar.value));
            canvas.drawText(valueText, cx, top - dp(4), textPaint);

            // rótulo abaixo
            canvas.drawText(bar.label, cx, getHeight() - dp(4), labelPaint);
        }
    }

    private int withAlpha(int color, int alpha) {
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
    }

    private float dp(float v) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, getResources().getDisplayMetrics());
    }

    private float sp(float v) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, v, getResources().getDisplayMetrics());
    }
}
