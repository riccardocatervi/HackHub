package hackhub.dto;

import java.util.UUID;

/**
 * DTO di risposta che conferma la creazione di un nuovo invito a unirsi al team.
 * Restituito al controller al termine del caso d'uso 'Invitare utente a unirsi al team'.
 */
public record InviteCreatedDTO(
        UUID idInvito,
        UUID idTeam,
        String nomeTeam,
        UUID idUtenteInvitato,
        String emailUtenteInvitato,
        String messaggio
) {
}
