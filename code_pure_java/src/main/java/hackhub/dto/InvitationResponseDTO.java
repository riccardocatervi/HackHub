package hackhub.dto;

import hackhub.model.StatoInvito;

import java.util.UUID;

/**
 * Risposta all'operazione di accettazione o rifiuto di un invito.
 * Restituita al termine del caso d'uso 'Accettare invito a unirsi al team'.
 */
public record InvitationResponseDTO(
        UUID idInvito,
        UUID idTeam,
        String nomeTeam,
        UUID idHackathon,
        String nomeHackathon,
        StatoInvito stato,
        String messaggio
) {
}
