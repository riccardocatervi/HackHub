package hackhub.dto;

import java.util.UUID;

/**
 * DTO per pre-popolare il form di pianificazione di una call.
 * Fornisce al mentore il contesto necessario (team, hackathon) prima di inserire
 * data, ora e descrizione della call.
 * Usato nel caso d'uso 'Pianificare call con un team'.
 */
public record CallFormDTO(
        UUID idRichiesta,
        UUID idMentore,
        String nomeTeam,
        String nomeHackathon
) {
}
