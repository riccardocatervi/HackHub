package hackhub.exception;

/**
 * Lanciata quando si verifica un errore di persistenza non recuperabile nel layer JDBC.
 * Fornisce un'astrazione tipizzata rispetto alle eccezioni SQL native.
 */
public class PersistenceException extends RuntimeException {

    public PersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
