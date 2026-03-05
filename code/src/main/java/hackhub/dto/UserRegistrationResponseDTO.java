package hackhub.dto;

import java.util.UUID;

/**
 * Risposta alla registrazione: conferma dell'account creato.
 */
public record UserRegistrationResponseDTO(
        UUID   id,
        String nome,
        String cognome,
        String email
) {}
