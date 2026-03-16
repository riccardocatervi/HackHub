package hackhub.exception;

import hackhub.model.StatoInvito;

/**
 * Lanciata quando si tenta un'operazione su un invito che si trova in uno stato
 * incompatibile con l'operazione richiesta (es. risposta a un invito già gestito).
 */
public class InvalidInvitationStateException extends RuntimeException {

    public InvalidInvitationStateException(StatoInvito statoAttuale) {
        super("Operazione non consentita: l'invito è già nello stato " + statoAttuale +
                ". Solo gli inviti IN_ATTESA possono essere accettati o rifiutati.");
    }
}
