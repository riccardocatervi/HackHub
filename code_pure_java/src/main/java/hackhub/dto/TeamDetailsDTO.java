package hackhub.dto;

import hackhub.model.entity.MembroTeam;
import hackhub.model.state.StatoHackathon;

import java.util.List;
import java.util.UUID;

/**
 * DTO con le informazioni dettagliate di un team.
 * Restituito al termine del caso d'uso 'Visualizzare team di appartenenza'
 * quando l'utente seleziona un team specifico per consultarne i dettagli.
 */
public record TeamDetailsDTO(
        UUID idTeam,
        String nomeTeam,
        String descrizioneTeam,
        UUID idLeader,
        UUID idHackathon,
        String nomeHackathon,
        StatoHackathon statoHackathon,
        List<MembroTeam> membri
) {
}
