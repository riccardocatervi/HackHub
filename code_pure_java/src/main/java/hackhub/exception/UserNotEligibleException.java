package hackhub.exception;

import java.util.UUID;

/**
 * Lanciata quando un utente non è idoneo a ricevere un invito:
 * è già membro del team oppure possiede già un invito in attesa per lo stesso team.
 */
public class UserNotEligibleException extends RuntimeException {

    public UserNotEligibleException(UUID idUtente, String motivo) {
        super("L'utente " + idUtente + " non è idoneo a ricevere l'invito: " + motivo);
    }
}
