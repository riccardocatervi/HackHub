package hackhub.model.entity;

import java.util.UUID;

/**
 * Rappresenta l'associazione tra un {@link Utente} e un {@link Team}.
 * Un'istanza di questa classe corrisponde a un utente che ha accettato
 * l'invito e fa parte attiva di un team per un determinato hackathon.
 * I dati anagrafici sono copiati dall'entità Utente al momento del caricamento
 * (lettura lazy tramite JOIN nel repository).
 */
public class MembroTeam {

    private final UUID idUtente;
    private final UUID idTeam;
    private final String nome;
    private final String cognome;
    private final String email;

    public MembroTeam(UUID idUtente, UUID idTeam, String nome, String cognome, String email) {
        this.idUtente = idUtente;
        this.idTeam = idTeam;
        this.nome = nome;
        this.cognome = cognome;
        this.email = email;
    }

    public UUID getIdUtente() {
        return idUtente;
    }

    public UUID getIdTeam() {
        return idTeam;
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
