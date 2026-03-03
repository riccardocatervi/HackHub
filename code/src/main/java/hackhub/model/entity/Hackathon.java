package hackhub.model.entity;

import hackhub.exception.IllegalStateTransitionException;
import hackhub.model.state.HackathonState;
import hackhub.model.state.StatoConcluso;
import hackhub.model.state.StatoHackathon;
import hackhub.model.state.StatoInCorso;
import hackhub.model.state.StatoInIscrizione;
import hackhub.model.state.StatoInValutazione;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Entità principale del sistema HackHub.
 * Agisce come Context nel pattern State: le operazioni sensibili allo stato
 * vengono delegate all'oggetto HackathonState corrente.
 */
public class Hackathon {

    private UUID id;
    private final String nome;
    private final LocalDateTime dataInizio;
    private final LocalDateTime dataFine;
    private final LocalDateTime scadenzaIscrizioni;
    private final LocalDateTime scadenzaSottomissioni;
    private final double premio;
    private final Address luogo;
    private final int dimensioneMaxTeam;
    private final String regolamento;
    private final UUID idOrganizzatore;
    private final UUID idGiudice;
    private final List<UUID> idMentori;
    private HackathonState stato;

    /**
     * Costruttore per la creazione di un nuovo Hackathon.
     * Lo stato iniziale è sempre IN_ISCRIZIONE (invariante del costruttore).
     */
    public Hackathon(String nome,
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
                     List<UUID> idMentori) {
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
        this.stato                 = new StatoInIscrizione();
    }

    /**
     * Costruttore per la ricostruzione dell'entità dal database.
     */
    public Hackathon(UUID id,
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
                     HackathonState stato) {
        this(nome, dataInizio, dataFine, scadenzaIscrizioni, scadenzaSottomissioni,
             premio, luogo, dimensioneMaxTeam, regolamento, idOrganizzatore, idGiudice, idMentori);
        this.id    = id;
        this.stato = stato;
    }

    // -------------------------------------------------------------------------
    // Metodi di business delegati al pattern State (GRASP: Information Expert)
    // -------------------------------------------------------------------------

    /**
     * Verifica che lo stato corrente consenta l'invio di una sottomissione.
     * Lancia IllegalStateTransitionException se non consentito.
     */
    public void verificaAccettaSottomissione() {
        stato.accettaSottomissione();
    }

    /**
     * Verifica che lo stato corrente consenta la proclamazione del vincitore.
     * Lancia IllegalStateTransitionException se non consentito.
     */
    public void verificaAccettaProclamazione() {
        stato.accettaProclamazione();
    }

    // -------------------------------------------------------------------------
    // Transizioni di stato
    // -------------------------------------------------------------------------

    /** IN_ISCRIZIONE → IN_CORSO */
    public void avvia() {
        if (!(stato instanceof StatoInIscrizione)) {
            throw new IllegalStateTransitionException(
                "Transizione non valida: l'hackathon non è in stato IN_ISCRIZIONE."
            );
        }
        this.stato = new StatoInCorso();
    }

    /** IN_CORSO → IN_VALUTAZIONE */
    public void chiudiSottomissioni() {
        if (!(stato instanceof StatoInCorso)) {
            throw new IllegalStateTransitionException(
                "Transizione non valida: l'hackathon non è in stato IN_CORSO."
            );
        }
        this.stato = new StatoInValutazione();
    }

    /** IN_VALUTAZIONE → CONCLUSO */
    public void concludi() {
        if (!(stato instanceof StatoInValutazione)) {
            throw new IllegalStateTransitionException(
                "Transizione non valida: l'hackathon non è in stato IN_VALUTAZIONE."
            );
        }
        this.stato = new StatoConcluso();
    }

    // -------------------------------------------------------------------------
    // Getter
    // -------------------------------------------------------------------------

    public UUID          getId()                    { return id; }
    public String        getNome()                  { return nome; }
    public LocalDateTime getDataInizio()            { return dataInizio; }
    public LocalDateTime getDataFine()              { return dataFine; }
    public LocalDateTime getScadenzaIscrizioni()    { return scadenzaIscrizioni; }
    public LocalDateTime getScadenzaSottomissioni() { return scadenzaSottomissioni; }
    public double        getPremio()                { return premio; }
    public Address       getLuogo()                 { return luogo; }
    public int           getDimensioneMaxTeam()     { return dimensioneMaxTeam; }
    public String        getRegolamento()           { return regolamento; }
    public UUID          getIdOrganizzatore()       { return idOrganizzatore; }
    public UUID          getIdGiudice()             { return idGiudice; }
    public List<UUID>    getIdMentori()             { return idMentori; }
    public HackathonState getStato()               { return stato; }
    public StatoHackathon getStatoEnum()           { return stato.getNome(); }

    public void setId(UUID id) { this.id = id; }
}
