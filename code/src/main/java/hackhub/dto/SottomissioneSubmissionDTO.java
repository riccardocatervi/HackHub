package hackhub.dto;

import java.util.UUID;

/**
 * DTO per la richiesta di invio di una sottomissione da parte di un team.
 */
public class SottomissioneSubmissionDTO {

    private UUID   idHackathon;
    private UUID   idTeam;
    private String linkRepository;
    private String linkDemo;
    private String descrizione;

    public SottomissioneSubmissionDTO() {}

    public UUID   getIdHackathon()    { return idHackathon; }
    public UUID   getIdTeam()         { return idTeam; }
    public String getLinkRepository() { return linkRepository; }
    public String getLinkDemo()       { return linkDemo; }
    public String getDescrizione()    { return descrizione; }

    public void setIdHackathon(UUID idHackathon)       { this.idHackathon = idHackathon; }
    public void setIdTeam(UUID idTeam)                 { this.idTeam = idTeam; }
    public void setLinkRepository(String linkRepository) { this.linkRepository = linkRepository; }
    public void setLinkDemo(String linkDemo)           { this.linkDemo = linkDemo; }
    public void setDescrizione(String descrizione)     { this.descrizione = descrizione; }
}
