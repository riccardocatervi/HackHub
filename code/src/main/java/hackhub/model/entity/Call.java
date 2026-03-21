package hackhub.model.entity;

import hackhub.model.StatoCall;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Entità che rappresenta una call pianificata tra un mentore e un team.
 * Viene creata al completamento del caso d'uso 'Pianificare call con un team',
 * a seguito dell'accettazione di una richiesta di supporto.
 * <p>
 * Estesa in it.5 con il campo {@code stato} per il caso d'uso
 * 'Gestire invito a call da parte di un mentore'.
 */
public class Call {

    private UUID id;
    private final UUID idRichiestaSupporto;
    private final LocalDate dataCall;
    private final LocalTime oraCall;
    private final String linkCall;
    private final String descrizione;
    private final LocalDateTime dataCreazione;

    /**
     * Stato corrente della call nel suo ciclo di vita.
     * Impostato a PENDENTE alla creazione; aggiornato alla risposta del leader.
     */
    private StatoCall stato;

    /**
     * Costruttore per una nuova call appena pianificata.
     * Il link è fornito dal sistema Calendar esterno.
     * L'id viene assegnato dal DB alla persistenza.
     * Stato iniziale: PENDENTE.
     */
    public Call(UUID idRichiestaSupporto,
                LocalDate dataCall,
                LocalTime oraCall,
                String linkCall,
                String descrizione) {
        this.idRichiestaSupporto = idRichiestaSupporto;
        this.dataCall = dataCall;
        this.oraCall = oraCall;
        this.linkCall = linkCall;
        this.descrizione = descrizione;
        this.dataCreazione = LocalDateTime.now();
        this.stato = StatoCall.PENDENTE;
    }

    /**
     * Costruttore originale per la ricostruzione della call dal database.
     * Imposta stato PENDENTE per retrocompatibilità con righe pre-it.5.
     */
    public Call(UUID id,
                UUID idRichiestaSupporto,
                LocalDate dataCall,
                LocalTime oraCall,
                String linkCall,
                String descrizione,
                LocalDateTime dataCreazione) {
        this.id = id;
        this.idRichiestaSupporto = idRichiestaSupporto;
        this.dataCall = dataCall;
        this.oraCall = oraCall;
        this.linkCall = linkCall;
        this.descrizione = descrizione;
        this.dataCreazione = dataCreazione;
        this.stato = StatoCall.PENDENTE;
    }

    /**
     * Costruttore esteso per la ricostruzione completa dal database (it.5).
     * Include lo stato della call.
     */
    public Call(UUID id,
                UUID idRichiestaSupporto,
                LocalDate dataCall,
                LocalTime oraCall,
                String linkCall,
                String descrizione,
                LocalDateTime dataCreazione,
                StatoCall stato) {
        this.id = id;
        this.idRichiestaSupporto = idRichiestaSupporto;
        this.dataCall = dataCall;
        this.oraCall = oraCall;
        this.linkCall = linkCall;
        this.descrizione = descrizione;
        this.dataCreazione = dataCreazione;
        this.stato = (stato != null) ? stato : StatoCall.PENDENTE;
    }

    public UUID getId() {
        return id;
    }

    /**
     * Chiamato dal repository dopo INSERT con RETURNING id.
     */
    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getIdRichiestaSupporto() {
        return idRichiestaSupporto;
    }

    public LocalDate getDataCall() {
        return dataCall;
    }

    public LocalTime getOraCall() {
        return oraCall;
    }

    public String getLinkCall() {
        return linkCall;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public LocalDateTime getDataCreazione() {
        return dataCreazione;
    }

    /**
     * Restituisce lo stato corrente della call.
     * Pattern Information Expert: l'entità è responsabile del proprio stato.
     */
    public StatoCall getStato() {
        return stato;
    }

    /**
     * Aggiorna lo stato della call.
     * Chiamato dal service al momento dell'accettazione o rifiuto da parte del leader.
     *
     * @param stato il nuovo stato (ACCETTATA o RIFIUTATA)
     */
    public void setStato(StatoCall stato) {
        this.stato = stato;
    }
}
