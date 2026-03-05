package hackhub.dto;

import java.util.UUID;

/**
 * DTO immutabile per il form di conferma della proclamazione del vincitore.
 */
public record ProclamazioneFormDTO(
        UUID idHackathon,
        String nomeHackathon,
        UUID idTeamVincitore,
        String nomeTeamVincitore,
        double premio
) {
}
