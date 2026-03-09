package hackhub.dto;

import hackhub.model.state.StatoHackathon;

import java.util.UUID;

/**
 * DTO immutabile di risposta dopo la proclamazione ufficiale del vincitore.
 */
public record ProclamazioneResponseDTO(
        UUID idHackathon,
        String nomeHackathon,
        UUID idTeamVincitore,
        String nomeTeamVincitore,
        double premio,
        StatoHackathon stato
) {
}
