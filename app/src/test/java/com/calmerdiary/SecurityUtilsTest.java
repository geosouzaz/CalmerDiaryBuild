package com.calmerdiary;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import com.calmerdiary.util.SecurityUtils;

import org.junit.Test;

/** Testes do hash de senha (RF05 — proteção por senha). */
public class SecurityUtilsTest {

    @Test
    public void senhaCorreta_verifica() {
        String salt = SecurityUtils.generateSalt();
        String hash = SecurityUtils.hashPassword("minhaSenha123", salt);
        assertTrue(SecurityUtils.verifyPassword("minhaSenha123", salt, hash));
    }

    @Test
    public void senhaIncorreta_naoVerifica() {
        String salt = SecurityUtils.generateSalt();
        String hash = SecurityUtils.hashPassword("minhaSenha123", salt);
        assertFalse(SecurityUtils.verifyPassword("senhaErrada", salt, hash));
    }

    @Test
    public void saltsDiferentes_geramHashesDiferentes() {
        String s1 = SecurityUtils.generateSalt();
        String s2 = SecurityUtils.generateSalt();
        String h1 = SecurityUtils.hashPassword("mesmaSenha", s1);
        String h2 = SecurityUtils.hashPassword("mesmaSenha", s2);
        assertNotEquals(h1, h2);
    }

    @Test
    public void hash_naoContemSenhaEmTextoPuro() {
        String salt = SecurityUtils.generateSalt();
        String hash = SecurityUtils.hashPassword("segredo", salt);
        assertFalse(hash.contains("segredo"));
    }
}
