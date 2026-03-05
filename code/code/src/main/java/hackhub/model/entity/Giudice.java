package hackhub.model.entity;

import java.util.UUID;

public class Giudice extends Attore {

    private final boolean disponibile;

    public Giudice(UUID id, String nome, String cognome, String email, boolean disponibile) {
        super(id, nome, cognome, email);
        this.disponibile = disponibile;
    }

    public boolean isDisponibile() {
        return disponibile;
    }
}
