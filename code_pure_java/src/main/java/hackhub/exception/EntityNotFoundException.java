package hackhub.exception;

/**
 * Eccezione generica lanciata quando un'entità richiesta non viene trovata.
 * Utilizzata nei contesti in cui non esiste un'eccezione specializzata per il tipo di entità.
 */
public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException(String message) {
        super(message);
    }
}
