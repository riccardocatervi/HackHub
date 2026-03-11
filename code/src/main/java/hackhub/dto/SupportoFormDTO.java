package hackhub.dto;

import java.util.UUID;

/**
 * DTO per pre-popolare il form di richiesta di supporto.
 * Fornisce il contesto necessario al leader prima di inserire il motivo.
 * Usato nel caso d'uso 'Inviare Richiesta di Supporto a un Mentore'.
 */
public record SupportoFormDTO(
        UUID idHackathon,
        String nomeHackathon,
        UUID idTeam,
        String nomeTeam
) { }
