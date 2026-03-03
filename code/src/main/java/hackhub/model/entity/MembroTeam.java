package hackhub.model.entity;

import java.util.UUID;

public class MembroTeam {

    private final UUID id;
    private final String nome;
    private final String cognome;
    private final String email;
    private final UUID idTeam;

    public MembroTeam(UUID id, String nome, String cognome, String email, UUID idTeam) {
        this.id = id;
        this.nome = nome;
        this.cognome = cognome;
        this.email = email;
        this.idTeam = idTeam;
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

    public UUID getIdTeam() {
        return idTeam;
    }
}
