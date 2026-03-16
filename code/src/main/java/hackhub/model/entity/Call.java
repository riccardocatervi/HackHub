package hackhub.model.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Entità che rappresenta una call pianificata tra un mentore e un team.
 * Viene creata al completamento del caso d'uso 'Pianificare call con un team',
 * a seguito dell'accettazione di una richiesta di supporto.
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
     * Costruttore per una nuova call appena pianificata.
     * Il link è fornito dal sistema Calendar esterno.
     * L'id viene assegnato dal DB alla persistenza.
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
    }

    /**
     * Costruttore per la ricostruzione della call dal database.
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
}
