package hackhub.dto;

import java.util.List;
import java.util.UUID;

/**
 * DTO immutabile con i dati necessari per popolare il modulo di segnalazione violazione.
 * Contiene l'elenco dei team dell'hackathon che il mentore può segnalare.
 */
public record ModuloSegnalazioneDTO(
        UUID hackathonId,
        String nomeHackathon,
        List<TeamSegnalabileItemDTO> teams
) {
}
