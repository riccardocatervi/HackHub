package hackhub.model.entity;

import java.util.List;
import java.util.UUID;

public class Team {

    private UUID id;           // assegnato dal DB al momento del salvataggio
    private final String nome;
    private final String descrizione;
    private final UUID idLeader;
    private final UUID idHackathon;
    private List<MembroTeam> membri;
    private boolean squalificato;

    public Team(UUID id, String nome, String descrizione, UUID idLeader, UUID idHackathon) {
        this.id = id;
        this.nome = nome;
        this.descrizione = descrizione;
        this.idLeader = idLeader;
        this.idHackathon = idHackathon;
        this.squalificato = false;
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

    public String getNome() {
        return nome;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public UUID getIdLeader() {
        return idLeader;
    }

    public UUID getIdHackathon() {
        return idHackathon;
    }

    public List<MembroTeam> getMembri() {
        return membri;
    }

    public void setMembri(List<MembroTeam> membri) {
        this.membri = membri;
    }

    public boolean isSqualificato() {
        return squalificato;
    }

    /**
     * Imposta il flag di squalifica ricevuto dal database durante la ricostruzione dell'entità.
     */
    public void setSqualificato(boolean squalificato) {
        this.squalificato = squalificato;
    }

    /**
     * Squalifica il team dall'hackathon.
     * Chiamato dal service quando l'organizzatore accetta una segnalazione di violazione.
     */
    public void squalifica() {
        this.squalificato = true;
    }
}
