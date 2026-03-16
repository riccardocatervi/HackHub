package hackhub.model.entity;

import hackhub.model.LivelloUrgenza;
import hackhub.model.StatoRichiesta;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entità che rappresenta una richiesta di supporto inviata dal leader di un team
 * a un mentore durante lo svolgimento dell'hackathon.
 * Usata nei casi d'uso 'Inviare Richiesta di Supporto a un Mentore'
 * e 'Prendere in carico una richiesta di supporto'.
 */
public class RichiestaSupporto {

    private UUID id;
    private final UUID idTeam;
    private final UUID idHackathon;
    private final UUID idMentore;
    private final String motivo;
    private final LocalDateTime dataInvio;

    // -------------------------------------------------------------------------
    // Campi aggiunti per il ciclo di vita della richiesta (it.4)
    // -------------------------------------------------------------------------

    /**
     * Stato corrente della richiesta nel suo ciclo di vita.
     */
    private StatoRichiesta stato;

    /**
     * Livello di urgenza impostato dal leader richiedente.
     */
    private LivelloUrgenza livelloUrgenza;

    /**
     * Motivazione del rifiuto, valorizzata solo quando lo stato è RESPINTA.
     */
    private String motivazioneRifiuto;

    // -------------------------------------------------------------------------
    // Costruttori (originali invariati + nuovi overload)
    // -------------------------------------------------------------------------

    /**
     * Costruttore originale per una nuova richiesta di supporto.
     * Imposta urgenza NORMALE e stato PENDENTE come valori predefiniti.
     * L'id e il timestamp vengono assegnati durante la persistenza.
     */
    public RichiestaSupporto(UUID idTeam,
                             UUID idHackathon,
                             UUID idMentore,
                             String motivo) {
        this.idTeam = idTeam;
        this.idHackathon = idHackathon;
        this.idMentore = idMentore;
        this.motivo = motivo;
        this.dataInvio = LocalDateTime.now();
        this.stato = StatoRichiesta.PENDENTE;
        this.livelloUrgenza = LivelloUrgenza.NORMALE;
    }

    /**
     * Costruttore esteso per una nuova richiesta di supporto con livello di urgenza specificato.
     * Aggiunto in it.4 senza modificare il costruttore originale (OCP).
     */
    public RichiestaSupporto(UUID idTeam,
                             UUID idHackathon,
                             UUID idMentore,
                             String motivo,
                             LivelloUrgenza livelloUrgenza) {
        this.idTeam = idTeam;
        this.idHackathon = idHackathon;
        this.idMentore = idMentore;
        this.motivo = motivo;
        this.dataInvio = LocalDateTime.now();
        this.stato = StatoRichiesta.PENDENTE;
        this.livelloUrgenza = (livelloUrgenza != null) ? livelloUrgenza : LivelloUrgenza.NORMALE;
    }

    /**
     * Costruttore originale per la ricostruzione della richiesta dal database.
     * Imposta stato PENDENTE e urgenza NORMALE per retrocompatibilità con righe pre-it.4.
     */
    public RichiestaSupporto(UUID id,
                             UUID idTeam,
                             UUID idHackathon,
                             UUID idMentore,
                             String motivo,
                             LocalDateTime dataInvio) {
        this.id = id;
        this.idTeam = idTeam;
        this.idHackathon = idHackathon;
        this.idMentore = idMentore;
        this.motivo = motivo;
        this.dataInvio = dataInvio;
        this.stato = StatoRichiesta.PENDENTE;
        this.livelloUrgenza = LivelloUrgenza.NORMALE;
    }

    /**
     * Costruttore esteso per la ricostruzione completa dal database (it.4).
     * Include stato, livello di urgenza e motivazione di rifiuto.
     */
    public RichiestaSupporto(UUID id,
                             UUID idTeam,
                             UUID idHackathon,
                             UUID idMentore,
                             String motivo,
                             LocalDateTime dataInvio,
                             StatoRichiesta stato,
                             LivelloUrgenza livelloUrgenza,
                             String motivazioneRifiuto) {
        this.id = id;
        this.idTeam = idTeam;
        this.idHackathon = idHackathon;
        this.idMentore = idMentore;
        this.motivo = motivo;
        this.dataInvio = dataInvio;
        this.stato = (stato != null) ? stato : StatoRichiesta.PENDENTE;
        this.livelloUrgenza = (livelloUrgenza != null) ? livelloUrgenza : LivelloUrgenza.NORMALE;
        this.motivazioneRifiuto = motivazioneRifiuto;
    }

    // -------------------------------------------------------------------------
    // Accessor originali (invariati)
    // -------------------------------------------------------------------------

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getIdTeam() {
        return idTeam;
    }

    public UUID getIdHackathon() {
        return idHackathon;
    }

    public UUID getIdMentore() {
        return idMentore;
    }

    public String getMotivo() {
        return motivo;
    }

    public LocalDateTime getDataInvio() {
        return dataInvio;
    }

    // -------------------------------------------------------------------------
    // Accessor per i nuovi campi (it.4)
    // -------------------------------------------------------------------------

    /**
     * Restituisce lo stato corrente della richiesta.
     * Pattern Information Expert: l'entità è responsabile del proprio stato.
     */
    public StatoRichiesta getStato() {
        return stato;
    }

    /**
     * Aggiorna lo stato della richiesta.
     * Chiamato dal service al momento della presa in carico o del rifiuto.
     */
    public void setStato(StatoRichiesta stato) {
        this.stato = stato;
    }

    public LivelloUrgenza getLivelloUrgenza() {
        return livelloUrgenza;
    }

    public void setLivelloUrgenza(LivelloUrgenza livelloUrgenza) {
        this.livelloUrgenza = livelloUrgenza;
    }

    /**
     * Restituisce la motivazione del rifiuto, valorizzata solo se stato == RESPINTA.
     */
    public String getMotivazioneRifiuto() {
        return motivazioneRifiuto;
    }

    /**
     * Imposta la motivazione del rifiuto.
     * Chiamato dal service contestualmente all'aggiornamento dello stato a RESPINTA.
     */
    public void setMotivazioneRifiuto(String motivazioneRifiuto) {
        this.motivazioneRifiuto = motivazioneRifiuto;
    }
}
