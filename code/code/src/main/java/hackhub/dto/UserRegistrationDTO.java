package hackhub.dto;

/**
 * Dati del modulo di registrazione locale (email + password).
 */
public record UserRegistrationDTO(
        String nome,
        String cognome,
        String email,
        String password
) {}
