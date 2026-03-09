package hackhub.model.entity;

/**
 * Value Object per l'indirizzo fisico di un Hackathon. Immutabile.
 */
public class Address {

    private final String via;
    private final int numeroCivico;
    private final String citta;
    private final String cap;
    private final String provincia;

    public Address(String via, int numeroCivico, String citta, String cap, String provincia) {
        this.via = via;
        this.numeroCivico = numeroCivico;
        this.citta = citta;
        this.cap = cap;
        this.provincia = provincia;
    }

    public String getVia() {
        return via;
    }

    public int getNumeroCivico() {
        return numeroCivico;
    }

    public String getCitta() {
        return citta;
    }

    public String getCap() {
        return cap;
    }

    public String getProvincia() {
        return provincia;
    }
}
