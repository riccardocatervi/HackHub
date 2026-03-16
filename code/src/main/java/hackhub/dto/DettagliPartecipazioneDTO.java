package hackhub.dto;

import hackhub.model.entity.MembroTeam;
import hackhub.model.state.StatoHackathon;

import java.util.List;
import java.util.UUID;

/**
 * Dati della partecipazione corrente di un membro a un team.
 * Restituito al termine di 'richiediDettagliPartecipazione' nel caso d'uso
 * 'Gestire iscrizione al team'.
 */
public record DettagliPartecipazioneDTO(
        UUID idTeam,
        String nomeTeam,
        String descrizioneTeam,
        UUID idLeader,
        UUID idHackathon,
        String nomeHackathon,
        StatoHackathon statoHackathon,
        boolean isLeader,
        boolean puoAbbandonare,
        List<MembroTeam> membri
) {
}
