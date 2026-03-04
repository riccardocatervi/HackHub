package hackhub.model.entity;

import java.util.UUID;

public class Valutazione {

    private final UUID id;
    private final double punteggio;
    private final String giudizioScritto;
    private final UUID idSottomissione;
    private final UUID idGiudice;

    public Valutazione(UUID id, double voto, String giudizioScritto,
                       UUID idSottomissione, UUID idGiudice) {
        this.id = id;
        this.punteggio = voto;
        this.giudizioScritto = giudizioScritto;
        this.idSottomissione = idSottomissione;
        this.idGiudice = idGiudice;
    }

    public UUID getId() {
        return id;
    }

    public double getPunteggio() {
        return punteggio;
    }

    public String getGiudizioScritto() {
        return giudizioScritto;
    }

    public UUID getIdSottomissione() {
        return idSottomissione;
    }

    public UUID getIdGiudice() {
        return idGiudice;
    }
}
