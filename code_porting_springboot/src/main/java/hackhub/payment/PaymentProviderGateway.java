package hackhub.payment;

import hackhub.dto.TransactionReceiptDTO;
import hackhub.exception.PaymentException;

/**
 * Porta (interfaccia) verso il payment provider esterno (es. Stripe, PayPal).
 * <p>
 * Applica il pattern Dependency Inversion (SOLID — DIP): il domain layer
 * dipende da questa astrazione, non dall'implementazione concreta del provider.
 * Ogni provider esterno viene adattato implementando questa interfaccia.
 */
public interface PaymentProviderGateway {

    /**
     * Esegue una transazione di pagamento verso l'indirizzo email del destinatario.
     *
     * @param request oggetto con i parametri della transazione
     * @return ricevuta della transazione completata
     * @throws PaymentException se la transazione fallisce lato provider
     */
    public TransactionReceiptDTO executeTransaction(PaymentRequest request);
}
