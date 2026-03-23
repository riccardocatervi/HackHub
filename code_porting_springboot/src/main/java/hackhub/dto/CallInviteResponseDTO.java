package hackhub.dto;

import hackhub.model.StatoCall;

import java.util.UUID;

/**
 * DTO di risposta all'operazione di accettazione o rifiuto di un invito a call.
 * Conferma l'esito dell'operazione con il nuovo stato della call.
 * Usato nel caso d'uso 'Gestire invito a call da parte di un mentore'.
 */
public record CallInviteResponseDTO(
        UUID idCall,
        UUID idTeam,
        StatoCall nuovoStato,
        String messaggio
) {
}
