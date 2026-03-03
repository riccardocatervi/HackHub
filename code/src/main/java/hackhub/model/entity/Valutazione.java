package hackhub.model.entity;

import java.util.UUID;

public class Valutazione {

    private final UUID   id;
    private final double voto;
    private final String giudizioScritto;
    private final UUID   idSottomissione;

    public Valutazione(UUID id, double voto, String giudizioScritto, UUID idSottomissione) {
        this.id              = id;
        this.voto            = voto;
        this.giudizioScritto = giudizioScritto;
        this.idSottomissione = idSottomissione;
    }

    public UUID   getId()              { return id; }
    public double getVoto()            { return voto; }
    public String getGiudizioScritto() { return giudizioScritto; }
    public UUID   getIdSottomissione() { return idSottomissione; }
}
