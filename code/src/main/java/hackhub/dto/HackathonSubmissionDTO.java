package hackhub.dto;

import hackhub.model.entity.Address;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO per la richiesta di creazione di un nuovo Hackathon.
 * Trasporta tutti i dati compilati dall'organizzatore nel form.
 */
public class HackathonSubmissionDTO {

    private UUID          idOrganizzatore;
    private UUID          idGiudice;
    private List<UUID>    idsMentori;
    private String        nome;
    private String        regolamento;
    private Address       luogo;
    private double        premio;
    private int           dimensioneMaxTeam;
    private LocalDateTime dataInizio;
    private LocalDateTime dataFine;
    private LocalDateTime scadenzaIscrizioni;
    private LocalDateTime scadenzaSottomissioni;

    public HackathonSubmissionDTO() {}

    public UUID          getIdOrganizzatore()       { return idOrganizzatore; }
    public UUID          getIdGiudice()             { return idGiudice; }
    public List<UUID>    getIdsMentori()            { return idsMentori; }
    public String        getNome()                  { return nome; }
    public String        getRegolamento()           { return regolamento; }
    public Address       getLuogo()                 { return luogo; }
    public double        getPremio()                { return premio; }
    public int           getDimensioneMaxTeam()     { return dimensioneMaxTeam; }
    public LocalDateTime getDataInizio()            { return dataInizio; }
    public LocalDateTime getDataFine()              { return dataFine; }
    public LocalDateTime getScadenzaIscrizioni()    { return scadenzaIscrizioni; }
    public LocalDateTime getScadenzaSottomissioni() { return scadenzaSottomissioni; }

    public void setIdOrganizzatore(UUID idOrganizzatore)             { this.idOrganizzatore = idOrganizzatore; }
    public void setIdGiudice(UUID idGiudice)                         { this.idGiudice = idGiudice; }
    public void setIdsMentori(List<UUID> idsMentori)                 { this.idsMentori = idsMentori; }
    public void setNome(String nome)                                 { this.nome = nome; }
    public void setRegolamento(String regolamento)                   { this.regolamento = regolamento; }
    public void setLuogo(Address luogo)                              { this.luogo = luogo; }
    public void setPremio(double premio)                             { this.premio = premio; }
    public void setDimensioneMaxTeam(int dimensioneMaxTeam)          { this.dimensioneMaxTeam = dimensioneMaxTeam; }
    public void setDataInizio(LocalDateTime dataInizio)              { this.dataInizio = dataInizio; }
    public void setDataFine(LocalDateTime dataFine)                  { this.dataFine = dataFine; }
    public void setScadenzaIscrizioni(LocalDateTime v)               { this.scadenzaIscrizioni = v; }
    public void setScadenzaSottomissioni(LocalDateTime v)            { this.scadenzaSottomissioni = v; }
}
