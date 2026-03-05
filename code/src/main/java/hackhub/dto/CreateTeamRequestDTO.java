package hackhub.dto;

import java.util.List;
import java.util.UUID;

/**
 * Richiesta di creazione di un nuovo team: dati inseriti dal leader nel form.
 */
public record CreateTeamRequestDTO(
        String nome,
        String descrizione,
        UUID idLeader,
        UUID idHackathon,
        List<UUID> idUtentiInvitati
) {
}
