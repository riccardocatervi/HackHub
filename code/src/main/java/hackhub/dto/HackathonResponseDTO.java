package hackhub.dto;

import hackhub.model.entity.Address;
import hackhub.model.state.StatoHackathon;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO di risposta dopo la creazione o il recupero di un Hackathon.
 */
public class HackathonResponseDTO {

    private final UUID          id;
    private final String        nome;
    private final LocalDateTime dataInizio;
    private final LocalDateTime dataFine;
    private final LocalDateTime scadenzaIscrizioni;
    private final LocalDateTime scadenzaSottomissioni;
    private final double        premio;
    private final Address       luogo;
    private final int           dimensioneMaxTeam;
    private final String        regolamento;
    private final UUID          idOrganizzatore;
    private final UUID          idGiudice;
    private final List<UUID>    idMentori;
    private final StatoHackathon stato;

    public HackathonResponseDTO(UUID id,
                                String nome,
                                LocalDateTime dataInizio,
                                LocalDateTime dataFine,
                                LocalDateTime scadenzaIscrizioni,
                                LocalDateTime scadenzaSottomissioni,
                                double premio,
                                Address luogo,
                                int dimensioneMaxTeam,
                                String regolamento,
                                UUID idOrganizzatore,
                                UUID idGiudice,
                                List<UUID> idMentori,
                                StatoHackathon stato) {
        this.id                    = id;
        this.nome                  = nome;
        this.dataInizio            = dataInizio;
        this.dataFine              = dataFine;
        this.scadenzaIscrizioni    = scadenzaIscrizioni;
        this.scadenzaSottomissioni = scadenzaSottomissioni;
        this.premio                = premio;
        this.luogo                 = luogo;
        this.dimensioneMaxTeam     = dimensioneMaxTeam;
        this.regolamento           = regolamento;
        this.idOrganizzatore       = idOrganizzatore;
        this.idGiudice             = idGiudice;
        this.idMentori             = idMentori;
        this.stato                 = stato;
    }

    public UUID           getId()                    { return id; }
    public String         getNome()                  { return nome; }
    public LocalDateTime  getDataInizio()            { return dataInizio; }
    public LocalDateTime  getDataFine()              { return dataFine; }
    public LocalDateTime  getScadenzaIscrizioni()    { return scadenzaIscrizioni; }
    public LocalDateTime  getScadenzaSottomissioni() { return scadenzaSottomissioni; }
    public double         getPremio()                { return premio; }
    public Address        getLuogo()                 { return luogo; }
    public int            getDimensioneMaxTeam()     { return dimensioneMaxTeam; }
    public String         getRegolamento()           { return regolamento; }
    public UUID           getIdOrganizzatore()       { return idOrganizzatore; }
    public UUID           getIdGiudice()             { return idGiudice; }
    public List<UUID>     getIdMentori()             { return idMentori; }
    public StatoHackathon getStato()                 { return stato; }
}
