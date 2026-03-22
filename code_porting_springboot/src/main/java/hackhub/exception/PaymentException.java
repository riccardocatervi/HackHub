package hackhub.exception;

/**
 * Lanciata quando si verifica un errore durante l'erogazione del premio tramite il payment provider.
 */
public class PaymentException extends RuntimeException {

    public PaymentException(String message) {
        super(message);
    }

    public PaymentException(String message, Throwable cause) {
        super(message, cause);
    }
}
