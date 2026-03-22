package hackhub.dto;

import java.util.UUID;

/**
 * Risposta all'operazione di abbandono del team.
 * Restituita al termine del caso d'uso 'Gestire iscrizione al team'.
 */
public record GestioneTeamResponseDTO(
        UUID idTeam,
        String nomeTeam,
        UUID idMembro,
        String messaggio
) {
}
