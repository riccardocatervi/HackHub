package hackhub.model.entity;

import java.util.UUID;

public class Mentore {

    private final UUID id;
    private final String nome;
    private final String cognome;
    private final boolean disponibile;

    public Mentore(UUID id, String nome, String cognome, boolean disponibile) {
        this.id = id;
        this.nome = nome;
        this.cognome = cognome;
        this.disponibile = disponibile;
    }

    public UUID getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getCognome() {
        return cognome;
    }

    public boolean isDisponibile() {
        return disponibile;
    }
}
