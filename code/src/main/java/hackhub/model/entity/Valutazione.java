package hackhub.model.entity;

import java.util.UUID;

/**
 * Entità che rappresenta la valutazione assegnata da un giudice
 * alla sottomissione di un team partecipante.
 */
public class Valutazione {

    private UUID id;
    private final double punteggio;
    private final String giudizioScritto;
    private final UUID idSottomissione;
    private final UUID idGiudice;

    /**
     * Costruttore per una nuova valutazione (id assegnato dal DB al momento del salvataggio).
     */
    public Valutazione(double punteggio, String giudizioScritto,
                       UUID idSottomissione, UUID idGiudice) {
        this.punteggio = punteggio;
        this.giudizioScritto = giudizioScritto;
        this.idSottomissione = idSottomissione;
        this.idGiudice = idGiudice;
    }

    /**
     * Costruttore per la ricostruzione dal database.
     */
    public Valutazione(UUID id, double punteggio, String giudizioScritto,
                       UUID idSottomissione, UUID idGiudice) {
        this(punteggio, giudizioScritto, idSottomissione, idGiudice);
        this.id = id;
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
