package hackhub.dto;

import java.util.UUID;

public class SottomissioneFormDTO {

    private final UUID idHackathon;
    private final String nomeHackathon;
    private final UUID idTeam;
    private final String nomeTeam;

    public SottomissioneFormDTO(UUID idHackathon, String nomeHackathon, UUID idTeam, String nomeTeam) {
        this.idHackathon = idHackathon;
        this.nomeHackathon = nomeHackathon;
        this.idTeam = idTeam;
        this.nomeTeam = nomeTeam;
    }

    public UUID getIdHackathon() {
        return idHackathon;
    }

    public String getNomeHackathon() {
        return nomeHackathon;
    }

    public UUID getIdTeam() {
        return idTeam;
    }

    public String getNomeTeam() {
        return nomeTeam;
    }
}
