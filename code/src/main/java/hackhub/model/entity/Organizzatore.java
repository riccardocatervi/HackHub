package hackhub.model.entity;

import java.util.UUID;

public class Organizzatore {

    private final UUID id;
    private final String nome;
    private final String cognome;
    private final String email;

    public Organizzatore(UUID id, String nome, String cognome, String email) {
        this.id = id;
        this.nome = nome;
        this.cognome = cognome;
        this.email = email;
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

    public String getEmail() {
        return email;
    }
}
