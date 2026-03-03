package hackhub.dto;

import hackhub.model.state.StatoHackathon;

import java.util.UUID;

/**
 * DTO di risposta dopo la proclamazione ufficiale del vincitore.
 * Contiene l'esito e lo stato finale CONCLUSO dell'hackathon.
 */
public class ProclamazioneResponseDTO {

    private final UUID          idHackathon;
    private final String        nomeHackathon;
    private final UUID          idTeamVincitore;
    private final String        nomeTeamVincitore;
    private final double        premio;
    private final StatoHackathon stato;

    public ProclamazioneResponseDTO(UUID idHackathon,
                                    String nomeHackathon,
                                    UUID idTeamVincitore,
                                    String nomeTeamVincitore,
                                    double premio,
                                    StatoHackathon stato) {
        this.idHackathon       = idHackathon;
        this.nomeHackathon     = nomeHackathon;
        this.idTeamVincitore   = idTeamVincitore;
        this.nomeTeamVincitore = nomeTeamVincitore;
        this.premio            = premio;
        this.stato             = stato;
    }

    public UUID          getIdHackathon()       { return idHackathon; }
    public String        getNomeHackathon()     { return nomeHackathon; }
    public UUID          getIdTeamVincitore()   { return idTeamVincitore; }
    public String        getNomeTeamVincitore() { return nomeTeamVincitore; }
    public double        getPremio()            { return premio; }
    public StatoHackathon getStato()            { return stato; }
}
