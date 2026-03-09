package hackhub.dto;

import java.util.UUID;

public class MentoreDTO {

    private final UUID id;
    private final String nome;
    private final String cognome;

    public MentoreDTO(UUID id, String nome, String cognome) {
        this.id = id;
        this.nome = nome;
        this.cognome = cognome;
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
}
