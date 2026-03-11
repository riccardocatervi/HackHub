package hackhub.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO immutabile che rappresenta la ricevuta restituita dal payment provider
 * dopo l'esecuzione di una transazione.
 *
 * @param transactionId       id univoco della transazione generato dal provider
 * @param emailDestinatario   indirizzo email a cui è stato inviato il pagamento
 * @param importo             importo trasferito in euro
 * @param timestamp           data e ora di completamento della transazione
 */
public record TransactionReceiptDTO(
        UUID transactionId,
        String emailDestinatario,
        double importo,
        LocalDateTime timestamp
) {
}
