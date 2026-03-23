package hackhub.dto;

import java.util.UUID;

/**
 * Risposta all'operazione di disiscrizione del team dall'hackathon.
 * Restituita al termine del caso d'uso 'Gestire iscrizione del team all'Hackathon'.
 */
public record UnsubscriptionSuccessDTO(
        UUID idHackathon,
        String nomeHackathon,
        UUID idTeam,
        String nomeTeam,
        String messaggio
) {
}
