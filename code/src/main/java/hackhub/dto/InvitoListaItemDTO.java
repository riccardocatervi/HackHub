package hackhub.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Elemento della lista degli inviti pendenti mostrata all'utente.
 * Contiene le informazioni essenziali per identificare il team e l'hackathon
 * a cui si è stati invitati.
 */
public record InvitoListaItemDTO(
        UUID idInvito,
        UUID idTeam,
        String nomeTeam,
        UUID idHackathon,
        String nomeHackathon,
        UUID idLeader,
        String nomeLeader,
        LocalDateTime dataInvio
) {
}
