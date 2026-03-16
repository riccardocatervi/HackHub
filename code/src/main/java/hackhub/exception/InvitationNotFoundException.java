package hackhub.exception;

import java.util.UUID;

/**
 * Lanciata quando un invito richiesto non esiste nel sistema.
 */
public class InvitationNotFoundException extends RuntimeException {

    public InvitationNotFoundException(UUID id) {
        super("Invito non trovato con id: " + id);
    }
}
