package hackhub.exception;

import java.util.UUID;

/**
 * Lanciata quando un utente non risulta membro di alcun team.
 */
public class NoTeamsFoundException extends RuntimeException {

    public NoTeamsFoundException(UUID idUtente) {
        super("L'utente " + idUtente + " non è membro di alcun team.");
    }
}
