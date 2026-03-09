package hackhub.dto;

import hackhub.model.StatoInvito;

import java.util.UUID;

/**
 * Risposta alla gestione di un invito: stato aggiornato dopo accettazione o rifiuto.
 */
public record InvitoResponseDTO(
        UUID idInvito,
        UUID idTeam,
        String nomeTeam,
        UUID idHackathon,
        String nomeHackathon,
        StatoInvito stato
) {
}
