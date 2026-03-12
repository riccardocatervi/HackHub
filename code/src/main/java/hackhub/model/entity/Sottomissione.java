package hackhub.model.entity;

import java.time.LocalDateTime;
import java.util.UUID;

public class Sottomissione {

    private UUID id;
    private String linkRepo;
    private String linkDemo;
    private String descrizione;
    private LocalDateTime dataInvio;
    private final UUID idTeam;
    private final UUID idHackathon;
    private boolean vincitore;
    private boolean valutato;

    /**
     * Costruttore per una nuova sottomissione (l'id verrà assegnato dal database).
     */
    public Sottomissione(String linkRepo,
                         String linkDemo,
                         String descrizione,
                         LocalDateTime dataInvio,
                         UUID idTeam,
                         UUID idHackathon) {
        this.linkRepo = linkRepo;
        this.linkDemo = linkDemo;
        this.descrizione = descrizione;
        this.dataInvio = dataInvio;
        this.idTeam = idTeam;
        this.idHackathon = idHackathon;
        this.vincitore = false;
        this.valutato = false;
    }

    /**
     * Costruttore per la ricostruzione dal database.
     */
    public Sottomissione(UUID id,
                         String linkRepo,
                         String linkDemo,
                         String descrizione,
                         LocalDateTime dataInvio,
                         UUID idTeam,
                         UUID idHackathon,
                         boolean vincitore,
                         boolean valutato) {
        this(linkRepo, linkDemo, descrizione, dataInvio, idTeam, idHackathon);
        this.id = id;
        this.vincitore = vincitore;
        this.valutato = valutato;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getLinkRepo() {
        return linkRepo;
    }

    public String getLinkDemo() {
        return linkDemo;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public LocalDateTime getDataInvio() {
        return dataInvio;
    }

    public UUID getIdTeam() {
        return idTeam;
    }

    public UUID getIdHackathon() {
        return idHackathon;
    }

    public boolean isVincitore() {
        return vincitore;
    }

    public boolean isValutato() {
        return valutato;
    }

    /**
     * Aggiorna i dettagli modificabili della sottomissione (GRASP Information Expert).
     * Registra il timestamp dell'aggiornamento come nuova data di invio.
     *
     * @param linkRepo    nuovo link al repository
     * @param linkDemo    nuovo link alla demo
     * @param descrizione nuova descrizione del progetto
     * @param timestamp   timestamp dell'aggiornamento
     */
    public void updateDetails(String linkRepo, String linkDemo,
                              String descrizione, LocalDateTime timestamp) {
        this.linkRepo = linkRepo;
        this.linkDemo = linkDemo;
        this.descrizione = descrizione;
        this.dataInvio = timestamp;
    }
}
