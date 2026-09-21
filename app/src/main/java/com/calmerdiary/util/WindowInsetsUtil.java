package com.calmerdiary.util;

import android.view.View;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Ajuda a lidar com o modo edge-to-edge (Android 15+): o app desenha atrás das
 * barras do sistema, então adicionamos o espaçamento das barras onde necessário
 * (ex.: topo da toolbar, base dos botões) para nada ficar sob a status bar ou a
 * barra de navegação.
 */
public final class WindowInsetsUtil {

    private WindowInsetsUtil() {
    }

    /** Adiciona o inset superior (status bar) ao padding do topo da view. */
    public static void applyTopInset(View view) {
        final int left = view.getPaddingLeft();
        final int top = view.getPaddingTop();
        final int right = view.getPaddingRight();
        final int bottom = view.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, windowInsets) -> {
            Insets bars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(left, top + bars.top, right, bottom);
            return windowInsets;
        });
        ViewCompat.requestApplyInsets(view);
    }

    /** Adiciona o inset inferior (barra de navegação/gestos) ao padding da base. */
    public static void applyBottomInset(View view) {
        final int left = view.getPaddingLeft();
        final int top = view.getPaddingTop();
        final int right = view.getPaddingRight();
        final int bottom = view.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, windowInsets) -> {
            Insets bars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(left, top, right, bottom + bars.bottom);
            return windowInsets;
        });
        ViewCompat.requestApplyInsets(view);
    }
}
