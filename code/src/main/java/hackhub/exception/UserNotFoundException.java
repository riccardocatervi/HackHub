package hackhub.exception;

import java.util.UUID;

/**
 * Lanciata quando un utente non viene trovato nel database.
 */
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(UUID id) {
        super("Utente non trovato con id: " + id);
    }

    public UserNotFoundException(String message) {
        super(message);
    }
}
