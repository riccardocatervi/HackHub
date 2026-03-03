package hackhub.dto;

import java.util.UUID;

/**
 * DTO per il form di conferma della proclamazione del vincitore.
 * Mostra all'organizzatore il team con il punteggio più alto prima della conferma.
 */
public class ProclamazioneFormDTO {

    private final UUID   idHackathon;
    private final String nomeHackathon;
    private final UUID   idTeamVincitore;
    private final String nomeTeamVincitore;
    private final double premio;

    public ProclamazioneFormDTO(UUID idHackathon,
                                String nomeHackathon,
                                UUID idTeamVincitore,
                                String nomeTeamVincitore,
                                double premio) {
        this.idHackathon       = idHackathon;
        this.nomeHackathon     = nomeHackathon;
        this.idTeamVincitore   = idTeamVincitore;
        this.nomeTeamVincitore = nomeTeamVincitore;
        this.premio            = premio;
    }

    public UUID   getIdHackathon()       { return idHackathon; }
    public String getNomeHackathon()     { return nomeHackathon; }
    public UUID   getIdTeamVincitore()   { return idTeamVincitore; }
    public String getNomeTeamVincitore() { return nomeTeamVincitore; }
    public double getPremio()            { return premio; }
}
