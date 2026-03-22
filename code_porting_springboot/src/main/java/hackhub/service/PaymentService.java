package hackhub.service;

import hackhub.dto.TransactionReceiptDTO;
import hackhub.exception.PaymentException;
import hackhub.payment.PaymentProviderGateway;
import hackhub.payment.PaymentRequest;

import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.logging.Logger;

/**
 * Servizio responsabile dell'erogazione del premio al leader del team vincitore.
 * <p>
 * Delega l'esecuzione della transazione al {@link PaymentProviderGateway},
 * rispettando il principio DIP: il servizio dipende dall'astrazione del gateway,
 * non dall'implementazione concreta del provider (Stripe, PayPal, mock, ecc.).
 */
@Service
public class PaymentService {

    private static final Logger LOG = Logger.getLogger(PaymentService.class.getName());

    private final PaymentProviderGateway paymentGateway;

    public PaymentService(PaymentProviderGateway paymentGateway) {
        this.paymentGateway = paymentGateway;
    }

    /**
     * Eroga il premio in denaro all'indirizzo email del leader del team vincitore.
     *
     * @param emailLeader indirizzo email del leader che riceve il premio
     * @param importo     importo del premio in euro
     * @param idHackathon id dell'hackathon di riferimento
     * @param idTeam      id del team vincitore
     * @return DTO di conferma con i dettagli della transazione completata
     * @throws PaymentException se la transazione fallisce lato provider
     */
    public TransactionReceiptDTO disbursePrize(String emailLeader, double importo,
                                               UUID idHackathon, UUID idTeam) {
        LOG.info(String.format(
                "Avvio erogazione premio — destinatario: %s | importo: €%.2f | hackathon: %s | team: %s",
                emailLeader, importo, idHackathon, idTeam
        ));

        PaymentRequest request = new PaymentRequest(emailLeader, importo, idHackathon, idTeam);

        try {
            TransactionReceiptDTO receipt = paymentGateway.executeTransaction(request);

            LOG.info(String.format(
                    "Premio erogato con successo — transazione: %s | destinatario: %s | importo: €%.2f",
                    receipt.transactionId(), receipt.emailDestinatario(), receipt.importo()
            ));

            return receipt;
        } catch (PaymentException e) {
            throw e;
        } catch (Exception e) {
            throw new PaymentException(
                    "Errore imprevisto durante l'erogazione del premio a " + emailLeader + ": " + e.getMessage(), e
            );
        }
    }
}
