package hackhub.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO immutabile di conferma dell'avvenuta erogazione del premio al leader del team vincitore.
 *
 * @param transactionId     id univoco della transazione restituito dal payment provider
 * @param emailDestinatario indirizzo email del leader a cui è stato inviato il premio
 * @param importo           importo erogato in euro
 * @param timestamp         data e ora del completamento del pagamento
 */
public record PaymentSuccessDTO(
        UUID transactionId,
        String emailDestinatario,
        double importo,
        LocalDateTime timestamp
) {
}
