package hackhub.model.entity;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entità che rappresenta una richiesta di supporto inviata dal leader di un team
 * a un mentore durante lo svolgimento dell'hackathon.
 * Usata nel caso d'uso 'Inviare Richiesta di Supporto a un Mentore'.
 */
public class RichiestaSupporto {

    private UUID id;
    private final UUID idTeam;
    private final UUID idHackathon;
    private final UUID idMentore;
    private final String motivo;
    private final LocalDateTime dataInvio;

    /**
     * Costruttore per una nuova richiesta di supporto.
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
    }

    /**
     * Costruttore per la ricostruzione della richiesta dal database.
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
    }

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
}
