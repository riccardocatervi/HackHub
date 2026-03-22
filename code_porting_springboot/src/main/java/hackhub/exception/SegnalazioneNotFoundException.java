package hackhub.exception;

import java.util.UUID;

/**
 * Lanciata quando una segnalazione non viene trovata nel repository.
 */
public class SegnalazioneNotFoundException extends RuntimeException {

    public SegnalazioneNotFoundException(UUID id) {
        super("Segnalazione non trovata con id: " + id);
    }
}
