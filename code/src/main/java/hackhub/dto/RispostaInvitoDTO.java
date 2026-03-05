package hackhub.dto;

import java.util.UUID;

/**
 * Risposta dell'utente invitato: accettazione o rifiuto dell'invito a unirsi al team.
 */
public record RispostaInvitoDTO(
        UUID idInvito,
        UUID idUtente,
        boolean accettato
) {
}
