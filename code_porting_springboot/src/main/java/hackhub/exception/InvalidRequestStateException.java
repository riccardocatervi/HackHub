package hackhub.exception;

import hackhub.model.StatoRichiesta;

/**
 * Lanciata quando si tenta di eseguire un'operazione su una richiesta di supporto
 * che non si trova nello stato atteso (es. accettare una richiesta già gestita).
 * Usata nel caso d'uso 'Prendere in carico una richiesta di supporto'.
 */
public class InvalidRequestStateException extends RuntimeException {

    public InvalidRequestStateException(StatoRichiesta statoAttuale) {
        super("Operazione non consentita: la richiesta di supporto è già nello stato '"
                + statoAttuale + "'. Solo le richieste PENDENTI possono essere gestite.");
    }
}
