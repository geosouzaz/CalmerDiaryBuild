package com.calmerdiary.util;

import android.content.Context;

import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

/**
 * Encapsula a autenticação biométrica (androidx.biometric).
 * Verifica disponibilidade e exibe o prompt do sistema.
 */
public final class BiometricHelper {

    public interface AuthCallback {
        void onSuccess();

        void onError(String message);
    }

    private static final int AUTHENTICATORS =
            BiometricManager.Authenticators.BIOMETRIC_WEAK;

    private BiometricHelper() {
    }

    /** true se o dispositivo tem biometria cadastrada e disponível. */
    public static boolean isAvailable(Context context) {
        BiometricManager manager = BiometricManager.from(context);
        int weak = manager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK);
        int strong = manager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG);
        return weak == BiometricManager.BIOMETRIC_SUCCESS
                || strong == BiometricManager.BIOMETRIC_SUCCESS;
    }

    public static void authenticate(FragmentActivity activity, String title, String subtitle,
                                    AuthCallback callback) {
        BiometricPrompt prompt = new BiometricPrompt(
                activity,
                ContextCompat.getMainExecutor(activity),
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                        callback.onSuccess();
                    }

                    @Override
                    public void onAuthenticationError(int errorCode, CharSequence errString) {
                        callback.onError(errString == null ? "" : errString.toString());
                    }
                });

        BiometricPrompt.PromptInfo info = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(title)
                .setSubtitle(subtitle)
                .setNegativeButtonText("Cancelar")
                .setAllowedAuthenticators(AUTHENTICATORS)
                .build();

        prompt.authenticate(info);
    }
}
