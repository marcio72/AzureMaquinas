package br.com.locaweb.relatorioclientes.clienteapp.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Hash de PIN para o login do app do cliente (telefone + PIN de 4 dígitos).
 * Mesmo padrão SHA-256 + salt por registro já usado no módulo instagramcheck,
 * reimplementado aqui de forma independente para manter os dois módulos
 * isolados (nada aqui depende do pacote instagramcheck, e vice-versa).
 */
public final class PinHashUtil {

    private static final String ALGORITMO = "SHA-256";
    private static final SecureRandom RANDOM = new SecureRandom();

    private PinHashUtil() {
    }

    /** Gera um salt aleatório novo, único por cliente. */
    public static String gerarSalt() {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /** Calcula o hash de "pin + salt". Guardar só o resultado disso, nunca o PIN puro. */
    public static String hash(String pin, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITMO);
            digest.update(Base64.getDecoder().decode(salt));
            byte[] resultado = digest.digest(pin.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(resultado);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 é garantido pela JVM padrão, isso nunca deve acontecer.
            throw new IllegalStateException("Algoritmo de hash indisponível: " + ALGORITMO, e);
        }
    }

    /** Confere se o PIN informado bate com o hash+salt armazenados. */
    public static boolean confere(String pinInformado, String saltArmazenado, String hashArmazenado) {
        if (pinInformado == null || saltArmazenado == null || hashArmazenado == null) {
            return false;
        }
        String hashCalculado = hash(pinInformado, saltArmazenado);
        return MessageDigest.isEqual(
                hashCalculado.getBytes(StandardCharsets.UTF_8),
                hashArmazenado.getBytes(StandardCharsets.UTF_8)
        );
    }

    /** Valida o formato do PIN: exatamente 4 dígitos numéricos. */
    public static boolean formatoValido(String pin) {
        return pin != null && pin.matches("\\d{4}");
    }
}
