package hackhub.exception;

import java.util.UUID;

public class HackathonNotFoundException extends RuntimeException {

    public HackathonNotFoundException(UUID id) {
        super("Hackathon non trovato con id: " + id);
    }

    public HackathonNotFoundException(String message) {
        super(message);
    }
}
