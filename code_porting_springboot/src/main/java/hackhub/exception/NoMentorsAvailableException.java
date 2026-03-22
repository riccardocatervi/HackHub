package hackhub.exception;

import java.util.UUID;

/**
 * Lanciata quando un hackathon non ha mentori associati disponibili
 * per accettare una richiesta di supporto.
 */
public class NoMentorsAvailableException extends RuntimeException {

    public NoMentorsAvailableException(UUID idHackathon) {
        super("Nessun mentore disponibile per l'hackathon con id: " + idHackathon);
    }
}
