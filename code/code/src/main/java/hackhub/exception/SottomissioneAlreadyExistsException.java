package hackhub.exception;

import java.util.UUID;

public class SottomissioneAlreadyExistsException extends RuntimeException {

    public SottomissioneAlreadyExistsException(UUID idHackathon, UUID idTeam) {
        super("Il team " + idTeam + " ha già inviato una sottomissione per l'hackathon " + idHackathon);
    }
}
