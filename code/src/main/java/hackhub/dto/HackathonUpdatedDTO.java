package hackhub.dto;

import java.util.List;
import java.util.UUID;

/**
 * DTO di risposta restituito dopo il salvataggio avvenuto con successo
 * delle modifiche alla lista dei mentori di un hackathon.
 */
public record HackathonUpdatedDTO(
        UUID idHackathon,
        String nomeHackathon,
        List<MentoreDTO> mentoriAttuali,
        String messaggio
) {
}
