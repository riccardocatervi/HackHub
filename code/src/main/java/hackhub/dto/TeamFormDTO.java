package hackhub.dto;

import java.util.List;
import java.util.UUID;

/**
 * Dati necessari per il form di creazione team:
 * informazioni sull'hackathon e lista degli utenti invitabili.
 */
public record TeamFormDTO(
        UUID idHackathon,
        String nomeHackathon,
        int dimensioneMaxTeam,
        List<UtenteDisponibileDTO> utentiDisponibili
) {
}
