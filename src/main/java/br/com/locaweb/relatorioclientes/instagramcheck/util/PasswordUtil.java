package br.com.locaweb.relatorioclientes.instagramcheck.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Hash de senha simples (SHA-256 + salt aleatório por usuário), sem
 * precisar adicionar nenhuma dependência nova ao projeto (nada de
 * Spring Security aqui — só java.security, que já vem no JDK).
 */
public final class PasswordUtil {

    private PasswordUtil() {
    }

    public static String gerarSalt() {
        byte[] bytes = new byte[16];
        new SecureRandom().nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }

    public static String hash(String senha, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(salt.getBytes());
            byte[] hashed = digest.digest(senha.getBytes());
            return Base64.getEncoder().encodeToString(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponível na JVM", e);
        }
    }

    public static boolean confere(String senha, String salt, String hashEsperado) {
        return hash(senha, salt).equals(hashEsperado);
    }
}
