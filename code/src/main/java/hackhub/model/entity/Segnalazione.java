package hackhub.model.entity;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entità che rappresenta una segnalazione di violazione del regolamento
 * inserita da un mentore nei confronti di un team partecipante.
 */
public class Segnalazione {

    private UUID id;
    private final UUID teamId;
    private final UUID mentoreId;
    private final UUID hackathonId;
    private final String descrizione;
    private final String prove;
    private final LocalDateTime dataInvio;

    /**
     * Costruttore per una nuova segnalazione (id e dataInvio assegnati dal DB).
     */
    public Segnalazione(UUID teamId, UUID mentoreId, UUID hackathonId,
                        String descrizione, String prove) {
        this.teamId      = teamId;
        this.mentoreId   = mentoreId;
        this.hackathonId = hackathonId;
        this.descrizione = descrizione;
        this.prove       = prove;
        this.dataInvio   = LocalDateTime.now();
    }

    /**
     * Costruttore per la ricostruzione dal database.
     */
    public Segnalazione(UUID id, UUID teamId, UUID mentoreId, UUID hackathonId,
                        String descrizione, String prove, LocalDateTime dataInvio) {
        this(teamId, mentoreId, hackathonId, descrizione, prove);
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

    /**
     * Chiamato dal repository dopo l'INSERT con RETURNING id.
     */
    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getTeamId() {
        return teamId;
    }

    public UUID getMentoreId() {
        return mentoreId;
    }

    public UUID getHackathonId() {
        return hackathonId;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public String getProve() {
        return prove;
    }

    public LocalDateTime getDataInvio() {
        return dataInvio;
    }
}
