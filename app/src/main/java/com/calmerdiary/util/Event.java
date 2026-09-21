package com.calmerdiary.util;

/**
 * Envelope para eventos de uso único expostos por LiveData
 * (ex.: navegar, mostrar erro) — evita reentrega ao girar a tela.
 */
public class Event<T> {

    private final T content;
    private boolean handled = false;

    public Event(T content) {
        this.content = content;
    }

    /** Retorna o conteúdo apenas na primeira vez; depois retorna null. */
    public T getIfNotHandled() {
        if (handled) {
            return null;
        }
        handled = true;
        return content;
    }

    public T peek() {
        return content;
    }
}
