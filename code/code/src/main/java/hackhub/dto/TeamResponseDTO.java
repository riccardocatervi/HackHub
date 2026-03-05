package hackhub.dto;

import java.util.List;
import java.util.UUID;

/**
 * Risposta alla creazione del team: conferma con i dati del team e degli utenti invitati.
 */
public record TeamResponseDTO(
        UUID id,
        String nome,
        String descrizione,
        UUID idLeader,
        UUID idHackathon,
        List<UUID> idUtentiInvitati
) {
}
