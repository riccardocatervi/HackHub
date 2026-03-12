package hackhub.dto;

import hackhub.model.StatoSegnalazione;

import java.util.UUID;

/**
 * DTO immutabile con la decisione dell'organizzatore su una segnalazione.
 * Il campo {@code azione} deve essere ACCETTATA (squalifica il team)
 * oppure RIFIUTATA (prove insufficienti).
 *
 * @param idSegnalazione id della segnalazione da gestire
 * @param azione         decisione dell'organizzatore: ACCETTATA o RIFIUTATA
 */
public record SegnalazioneGestioneDTO(
        UUID idSegnalazione,
        StatoSegnalazione azione
) {
}
