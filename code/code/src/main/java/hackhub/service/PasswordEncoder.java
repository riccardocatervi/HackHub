package hackhub.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Codifica e verifica password tramite SHA-256 con salt casuale.
 * Formato memorizzato: {@code base64(salt):base64(hash)}.
 * <p>
 * Progettato per essere sostituito da BCrypt o Argon2 in produzione;
 * in migrazione Spring Boot è sufficiente decorare con {@code @Bean}.
 */
public class PasswordEncoder {

    private static final String ALGORITMO = "SHA-256";

    /**
     * Codifica una password in chiaro applicando un salt casuale.
     *
     * @param plaintext password in chiaro
     * @return stringa codificata nel formato {@code base64(salt):base64(hash)}
     */
    public String encode(String plaintext) {
        try {
            byte[] salt = generaSalt();
            byte[] hash = calcola(plaintext, salt);
            return Base64.getEncoder().encodeToString(salt)
                    + ":" + Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Algoritmo di hashing non disponibile: " + ALGORITMO, e);
        }
    }

    /**
     * Verifica se una password in chiaro corrisponde all'hash memorizzato.
     *
     * @param plaintext password in chiaro da verificare
     * @param encoded   stringa codificata prodotta da {@link #encode(String)}
     * @return {@code true} se la password corrisponde
     */
    public boolean matches(String plaintext, String encoded) {
        try {
            String[] parti = encoded.split(":");
            if (parti.length != 2) return false;

            byte[] salt = Base64.getDecoder().decode(parti[0]);
            byte[] hashAtteso = Base64.getDecoder().decode(parti[1]);
            byte[] hashCalc = calcola(plaintext, salt);

            return MessageDigest.isEqual(hashAtteso, hashCalc);
        } catch (NoSuchAlgorithmException | IllegalArgumentException e) {
            return false;
        }
    }

    private byte[] generaSalt() throws NoSuchAlgorithmException {
        SecureRandom rng = SecureRandom.getInstanceStrong();
        byte[] salt = new byte[16];
        rng.nextBytes(salt);
        return salt;
    }

    private byte[] calcola(String plaintext, byte[] salt) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(ALGORITMO);
        md.update(salt);
        return md.digest(plaintext.getBytes(StandardCharsets.UTF_8));
    }
}
