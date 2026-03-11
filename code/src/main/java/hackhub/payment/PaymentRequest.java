package hackhub.payment;

import java.util.UUID;

/**
 * Value Object che incapsula i parametri necessari per eseguire una transazione
 * tramite il payment provider esterno.
 * Immutabile per garantire la coerenza dei dati durante l'elaborazione.
 */
public class PaymentRequest {

    private final String emailDestinatario;
    private final double importo;
    private final UUID idHackathon;
    private final UUID idTeam;

    public PaymentRequest(String emailDestinatario, double importo,
                          UUID idHackathon, UUID idTeam) {
        this.emailDestinatario = emailDestinatario;
        this.importo           = importo;
        this.idHackathon       = idHackathon;
        this.idTeam            = idTeam;
    }

    public String getEmailDestinatario() {
        return emailDestinatario;
    }

    public double getImporto() {
        return importo;
    }

    public UUID getIdHackathon() {
        return idHackathon;
    }

    public UUID getIdTeam() {
        return idTeam;
    }
}
