package hackhub.model.entity;

import java.util.UUID;

/**
 * Rappresenta l'associazione tra un {@link Utente} e un {@link Team}.
 * Un'istanza di questa classe corrisponde a un utente che ha accettato
 * l'invito e fa parte attiva di un team per un determinato hackathon.
 * I dati anagrafici sono copiati dall'entità Utente al momento del caricamento
 * (lettura lazy tramite JOIN nel repository).
 */
public class MembroTeam extends Attore {

    private final UUID idTeam;

    public MembroTeam(UUID idUtente, UUID idTeam, String nome, String cognome, String email) {
        super(idUtente, nome, cognome, email);
        this.idTeam = idTeam;
    }

    public UUID getIdTeam() {
        return idTeam;
    }

}
