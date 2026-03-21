package hackhub.dto;

import hackhub.model.state.StatoHackathon;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO sintetico per la visualizzazione di un hackathon in una lista.
 * Contiene solo i dati essenziali per l'identificazione e selezione dell'evento.
 */
public record HackathonSummaryDTO(
        UUID id,
        String nome,
        StatoHackathon stato,
        LocalDateTime dataInizio,
        LocalDateTime dataFine
) {
}
