package hackhub.payment;

import hackhub.dto.TransactionReceiptDTO;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Implementazione simulata (mock) del gateway di pagamento.
 * <p>
 * Sostituisce l'integrazione reale con provider come Stripe o PayPal
 * finché le API a pagamento non saranno attive. Registra l'operazione
 * tramite log di sistema e restituisce una ricevuta con id transazione generato localmente.
 * <p>
 * Per passare all'integrazione reale è sufficiente sostituire questa classe
 * con un'implementazione concreta di {@link PaymentProviderGateway}, senza
 * modificare alcun altro componente, rispettando così isl principio OCP.
 */
public class MockPaymentProviderGateway implements PaymentProviderGateway {

    private static final Logger LOG = Logger.getLogger(MockPaymentProviderGateway.class.getName());

    @Override
    public TransactionReceiptDTO executeTransaction(PaymentRequest request) {
        UUID transactionId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        LOG.info(String.format(
                "[MOCK PAYMENT] Transazione simulata — id: %s | destinatario: %s | " +
                "importo: €%.2f | hackathon: %s | team: %s | timestamp: %s",
                transactionId,
                request.getEmailDestinatario(),
                request.getImporto(),
                request.getIdHackathon(),
                request.getIdTeam(),
                now
        ));

        return new TransactionReceiptDTO(
                transactionId,
                request.getEmailDestinatario(),
                request.getImporto(),
                now
        );
    }
}
