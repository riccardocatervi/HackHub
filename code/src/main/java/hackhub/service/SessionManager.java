package hackhub.service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gestore delle sessioni utente in memoria.
 * Associa ogni token di sessione (UUID stringa) all'identificativo dell'utente autenticato.
 * <p>
 * In migrazione Spring Boot: sostituire con gestione sessioni server-side (HttpSession)
 * o token JWT tramite Spring Security.
 */
public class SessionManager {

    private final Map<String, UUID> sessioni = new ConcurrentHashMap<>();

    /**
     * Crea una nuova sessione per l'utente indicato e restituisce il token generato.
     *
     * @param idUtente l'id dell'utente autenticato
     * @return token di sessione univoco
     */
    public String creaSessione(UUID idUtente) {
        String token = UUID.randomUUID().toString();
        sessioni.put(token, idUtente);
        return token;
    }

    /**
     * Restituisce l'id dell'utente associato al token di sessione, se valido.
     *
     * @param token il token di sessione da verificare
     * @return l'id dell'utente o {@link Optional#empty()} se il token non è valido
     */
    public Optional<UUID> trovaPerId(String token) {
        return Optional.ofNullable(sessioni.get(token));
    }

    /**
     * Invalida il token di sessione (logout).
     *
     * @param token il token di sessione da invalidare
     */
    public void invalidaSessione(String token) {
        sessioni.remove(token);
    }
}
