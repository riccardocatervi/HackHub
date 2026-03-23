package hackhub.exception;

/**
 * Lanciata quando un utente tenta di eseguire un'azione per la quale
 * non possiede i permessi necessari (es. disiscrizione di un team da parte
 * di un membro che non è il leader).
 */
public class UnauthorizedActionException extends RuntimeException {

    public UnauthorizedActionException(String message) {
        super(message);
    }
}
