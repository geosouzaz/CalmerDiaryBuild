package com.calmerdiary.util;

/**
 * Callback genérico para operações assíncronas de repositório.
 * O resultado é sempre entregue na thread principal.
 */
public interface Callback<T> {
    void onSuccess(T result);

    void onError(Exception error);
}
