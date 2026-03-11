package hackhub.dto;

import hackhub.model.StatoSegnalazione;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO immutabile con i dettagli completi di una segnalazione.
 * Restituito all'organizzatore quando seleziona una segnalazione dalla lista.
 *
 * @param id            id univoco della segnalazione
 * @param teamId        id del team segnalato
 * @param nomeTeam      nome del team segnalato
 * @param mentoreId     id del mentore che ha inviato la segnalazione
 * @param nomeMentore   nome completo del mentore (può essere null se non disponibile)
 * @param descrizione   descrizione della violazione rilevata
 * @param prove         eventuali prove a supporto (può essere null)
 * @param dataInvio     timestamp di invio della segnalazione
 * @param stato         stato corrente della segnalazione
 */
public record SegnalazioneDettagliDTO(
        UUID id,
        UUID teamId,
        String nomeTeam,
        UUID mentoreId,
        String nomeMentore,
        String descrizione,
        String prove,
        LocalDateTime dataInvio,
        StatoSegnalazione stato
) {
}
