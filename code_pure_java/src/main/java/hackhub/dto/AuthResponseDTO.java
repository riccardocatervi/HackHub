package hackhub.dto;

import java.util.UUID;

/**
 * DTO immutabile restituito al termine di un'autenticazione riuscita.
 * Contiene i dati identificativi dell'utente e il token di sessione
 * da utilizzare per le successive richieste autenticate.
 */
public record AuthResponseDTO(
        UUID idUtente,
        String nome,
        String cognome,
        String email,
        String sessionToken
) {
}
