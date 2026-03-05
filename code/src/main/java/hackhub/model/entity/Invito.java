package hackhub.model.entity;

import hackhub.model.StatoInvito;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Invito inviato da un leader a un utente registrato affinché si unisca al suo team.
 * Lo stato dell'invito segue il ciclo: IN_ATTESA → ACCETTATO | RIFIUTATO.
 */
public class Invito {

    private UUID id;           // assegnato dal DB
    private UUID idTeam;       // valorizzato dal servizio dopo il salvataggio del team
    private final UUID idUtente;
    private final UUID idHackathon;
    private StatoInvito stato;
    private final LocalDateTime dataInvio;

    /**
     * Costruttore per un nuovo invito (id e dataInvio assegnati dal DB).
     */
    public Invito(UUID idTeam, UUID idUtente, UUID idHackathon) {
        this.idTeam = idTeam;
        this.idUtente = idUtente;
        this.idHackathon = idHackathon;
        this.stato = StatoInvito.IN_ATTESA;
        this.dataInvio = LocalDateTime.now();
    }

    /**
     * Costruttore per la ricostruzione dal database.
     */
    public Invito(UUID id, UUID idTeam, UUID idUtente, UUID idHackathon,
                  StatoInvito stato, LocalDateTime dataInvio) {
        this.id = id;
        this.idTeam = idTeam;
        this.idUtente = idUtente;
        this.idHackathon = idHackathon;
        this.stato = stato;
        this.dataInvio = dataInvio;
    }

    public UUID getId() {
        return id;
    }

    /**
     * Chiamato dal repository dopo l'INSERT con RETURNING id.
     */
    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getIdTeam() {
        return idTeam;
    }

    /**
     * Chiamato dal servizio dopo che il team è stato persistito e ha ricevuto il suo id.
     */
    public void setIdTeam(UUID idTeam) {
        this.idTeam = idTeam;
    }

    public UUID getIdUtente() {
        return idUtente;
    }

    public UUID getIdHackathon() {
        return idHackathon;
    }

    public StatoInvito getStato() {
        return stato;
    }

    public void setStato(StatoInvito stato) {
        this.stato = stato;
    }

    public LocalDateTime getDataInvio() {
        return dataInvio;
    }
}
