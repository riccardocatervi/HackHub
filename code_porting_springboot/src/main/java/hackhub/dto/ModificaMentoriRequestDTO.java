package hackhub.dto;

import java.util.List;
import java.util.UUID;

/**
 * DTO immutabile che trasporta le modifiche alla lista mentori di un hackathon.
 * Contiene la lista degli ID dei mentori da aggiungere e quella dei mentori da rimuovere.
 */
public record ModificaMentoriRequestDTO(
        UUID idHackathon,
        List<UUID> idMentoriDaAggiungere,
        List<UUID> idMentoriDaRimuovere
) {
}
