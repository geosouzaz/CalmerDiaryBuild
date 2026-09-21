package com.calmerdiary.util;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Utilitário de segurança para senhas.
 * Nunca armazenamos a senha em texto puro: guardamos um hash PBKDF2
 * (HMAC-SHA256) com um "salt" aleatório por usuário.
 */
public final class SecurityUtils {

    private static final int ITERATIONS = 120_000;
    private static final int KEY_LENGTH_BITS = 256;
    private static final int SALT_LENGTH_BYTES = 16;
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";

    private SecurityUtils() {
        // classe utilitária — sem instâncias
    }

    /** Gera um salt aleatório codificado em Base64. */
    public static String generateSalt() {
        byte[] salt = new byte[SALT_LENGTH_BYTES];
        new SecureRandom().nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /** Calcula o hash PBKDF2 da senha usando o salt informado (Base64). */
    public static String hashPassword(String password, String saltBase64) {
        try {
            byte[] salt = Base64.getDecoder().decode(saltBase64);
            PBEKeySpec spec = new PBEKeySpec(
                    password.toCharArray(), salt, ITERATIONS, KEY_LENGTH_BITS);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            byte[] hash = factory.generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Falha ao gerar hash da senha", e);
        }
    }

    /**
     * Verifica se a senha corresponde ao hash esperado.
     * Comparação em tempo constante para evitar ataques de temporização.
     */
    public static boolean verifyPassword(String password, String saltBase64, String expectedHashBase64) {
        String actual = hashPassword(password, saltBase64);
        return constantTimeEquals(actual, expectedHashBase64);
    }

    private static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null || a.length() != b.length()) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }
}
