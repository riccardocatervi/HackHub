package hackhub.exception;

import java.util.UUID;

/**
 * Lanciata quando una richiesta di supporto non viene trovata nel sistema
 * tramite il suo identificatore univoco.
 * Usata nei casi d'uso 'Prendere in carico una richiesta di supporto'
 * e 'Pianificare call con un team'.
 */
public class SupportRequestNotFoundException extends RuntimeException {

    public SupportRequestNotFoundException(UUID idRichiesta) {
        super("Richiesta di supporto non trovata con id: " + idRichiesta);
    }
}
