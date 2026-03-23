package hackhub.exception;

/**
 * Lanciata quando le credenziali fornite dall'utente non sono valide:
 * l'account non esiste oppure la password non corrisponde.
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException(String message) {
        super(message);
    }
}
