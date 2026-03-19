package hackhub.dto;

import hackhub.model.state.StatoHackathon;

import java.util.UUID;

/**
 * DTO sintetico per la visualizzazione di un team nella lista dei team di appartenenza.
 * Contiene i dati essenziali del team e dell'hackathon associato.
 */
public record TeamSummaryDTO(
        UUID idTeam,
        String nomeTeam,
        UUID idHackathon,
        String nomeHackathon,
        StatoHackathon statoHackathon,
        boolean isLeader
) {
}
