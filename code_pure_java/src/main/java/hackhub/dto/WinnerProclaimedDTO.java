package hackhub.dto;

import java.util.UUID;

/**
 * DTO immutabile che aggrega le informazioni sull'hackathon concluso
 * con vincitore proclamato e stato dell'erogazione del premio.
 * Utilizzato internamente per coordinare le notifiche ai partecipanti.
 *
 * @param idHackathon       id dell'hackathon concluso
 * @param nomeHackathon     nome dell'hackathon
 * @param idTeamVincitore   id del team vincitore
 * @param nomeTeamVincitore nome del team vincitore
 * @param premio            importo del premio in euro
 * @param premioDisbursed   true se il premio è stato erogato con successo
 */
public record WinnerProclaimedDTO(
        UUID idHackathon,
        String nomeHackathon,
        UUID idTeamVincitore,
        String nomeTeamVincitore,
        double premio,
        boolean premioDisbursed
) {
}
