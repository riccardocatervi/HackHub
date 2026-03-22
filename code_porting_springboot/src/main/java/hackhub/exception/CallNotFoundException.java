package hackhub.exception;

import java.util.UUID;

/**
 * Lanciata quando una call non viene trovata nel sistema tramite il suo identificativo.
 * Usata nel caso d'uso 'Gestire invito a call da parte di un mentore'.
 */
public class CallNotFoundException extends RuntimeException {

    public CallNotFoundException(UUID idCall) {
        super("Call non trovata con id: " + idCall);
    }

    public CallNotFoundException(String message) {
        super(message);
    }
}
