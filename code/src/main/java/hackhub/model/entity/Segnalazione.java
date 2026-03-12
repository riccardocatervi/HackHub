package hackhub.model.entity;

import hackhub.model.StatoSegnalazione;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entità che rappresenta una segnalazione di violazione del regolamento
 * inserita da un mentore nei confronti di un team partecipante.
 * <p>
 * Il campo {@code stato} traccia il ciclo di vita della segnalazione:
 * PENDENTE → ACCETTATA (team squalificato) oppure PENDENTE → RIFIUTATA.
 */
public class Segnalazione {

    private UUID id;
    private final UUID teamId;
    private final UUID mentoreId;
    private final UUID hackathonId;
    private final String descrizione;
    private final String prove;
    private final LocalDateTime dataInvio;
    private StatoSegnalazione stato;

    /**
     * Costruttore per una nuova segnalazione (id e dataInvio assegnati dal DB).
     * Lo stato iniziale è sempre PENDENTE.
     */
    public Segnalazione(UUID teamId, UUID mentoreId, UUID hackathonId,
                        String descrizione, String prove) {
        this.teamId = teamId;
        this.mentoreId = mentoreId;
        this.hackathonId = hackathonId;
        this.descrizione = descrizione;
        this.prove = prove;
        this.dataInvio = LocalDateTime.now();
        this.stato = StatoSegnalazione.PENDENTE;
    }

    /**
     * Costruttore per la ricostruzione dal database (senza stato — retro-compatibilità).
     * Lo stato viene impostato a PENDENTE come valore di default.
     */
    public Segnalazione(UUID id, UUID teamId, UUID mentoreId, UUID hackathonId,
                        String descrizione, String prove, LocalDateTime dataInvio) {
        this(teamId, mentoreId, hackathonId, descrizione, prove);
        this.id = id;
    }

    /**
     * Costruttore per la ricostruzione dal database con stato esplicito.
     * Utilizzato dal repository JDBC per ricostruire l'entità completa.
     */
    public Segnalazione(UUID id, UUID teamId, UUID mentoreId, UUID hackathonId,
                        String descrizione, String prove, LocalDateTime dataInvio,
                        StatoSegnalazione stato) {
        this(teamId, mentoreId, hackathonId, descrizione, prove);
        this.id = id;
        this.stato = stato;
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

    public StatoSegnalazione getStato() {
        return stato;
    }

    /**
     * Aggiorna lo stato della segnalazione.
     * Chiamato dal service durante la gestione della segnalazione.
     */
    public void setStato(StatoSegnalazione stato) {
        this.stato = stato;
    }
}
