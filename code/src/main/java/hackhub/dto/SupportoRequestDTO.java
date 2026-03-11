package hackhub.dto;

import java.util.UUID;

/**
 * DTO con il payload della richiesta di supporto compilata dal leader del team.
 * Usato nel caso d'uso 'Inviare Richiesta di Supporto a un Mentore'.
 */
public record SupportoRequestDTO(
        UUID idHackathon,
        UUID idTeam,
        String motivo
) { }
