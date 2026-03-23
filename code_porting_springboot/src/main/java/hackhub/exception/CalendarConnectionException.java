package hackhub.exception;

/**
 * Lanciata quando il sistema non riesce a stabilire la connessione con
 * il servizio di Calendar esterno durante la pianificazione di una call.
 * Usata nel caso d'uso 'Pianificare call con un team'.
 */
public class CalendarConnectionException extends RuntimeException {

    public CalendarConnectionException(String message) {
        super(message);
    }

    public CalendarConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
