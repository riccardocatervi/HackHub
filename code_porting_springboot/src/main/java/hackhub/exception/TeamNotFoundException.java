package hackhub.exception;

import java.util.UUID;

public class TeamNotFoundException extends RuntimeException {

    public TeamNotFoundException(UUID id) {
        super("Team non trovato con id: " + id);
    }
}
