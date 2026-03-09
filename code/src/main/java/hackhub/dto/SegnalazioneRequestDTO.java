package hackhub.dto;

import java.util.UUID;

/**
 * DTO immutabile con i dati compilati dal mentore nel modulo di segnalazione violazione.
 *
 * @param hackathonId  id dell'hackathon in cui si è verificata la violazione
 * @param mentoreId    id del mentore che effettua la segnalazione
 * @param teamId       id del team segnalato
 * @param descrizione  descrizione della violazione rilevata
 * @param prove        eventuali prove a supporto della segnalazione (può essere null)
 */
public record SegnalazioneRequestDTO(
        UUID hackathonId,
        UUID mentoreId,
        UUID teamId,
        String descrizione,
        String prove
) {
}
