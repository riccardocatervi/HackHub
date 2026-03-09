package hackhub.dto;

import hackhub.model.entity.Address;
import hackhub.model.state.StatoHackathon;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO immutabile di risposta dopo la creazione o il recupero di un hackathon.
 */
public record HackathonResponseDTO(
        UUID id,
        String nome,
        LocalDateTime dataInizio,
        LocalDateTime dataFine,
        LocalDateTime scadenzaIscrizioni,
        LocalDateTime scadenzaSottomissioni,
        double premio,
        Address luogo,
        int dimensioneMaxTeam,
        String regolamento,
        UUID idOrganizzatore,
        UUID idGiudice,
        List<UUID> idMentori,
        StatoHackathon stato
) {
}
