package hackhub.model.entity;

import java.util.UUID;

/**
 * Classe astratta base per tutti gli attori della piattaforma (staff).
 * Raccoglie gli attributi comuni: id, nome, cognome, email.
 * <p>
 * Nota: {@code Attore} non è un {@code Utente} registrato alla piattaforma —
 * rappresenta il personale operativo (organizzatori, giudici, mentori).
 */
public abstract class Attore {

    private final UUID id;
    private final String nome;
    private final String cognome;
    private final String email;

    protected Attore(UUID id, String nome, String cognome, String email) {
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
