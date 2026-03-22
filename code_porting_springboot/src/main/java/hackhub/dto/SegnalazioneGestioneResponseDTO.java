package hackhub.dto;

import hackhub.model.StatoSegnalazione;

import java.util.UUID;

/**
 * DTO immutabile di risposta dopo la gestione di una segnalazione da parte dell'organizzatore.
 *
 * @param idSegnalazione   id della segnalazione gestita
 * @param stato            nuovo stato della segnalazione (ACCETTATA o RIFIUTATA)
 * @param teamSqualificato true se il team è stato squalificato a seguito dell'accettazione
 * @param messaggio        messaggio di conferma per l'organizzatore
 */
public record SegnalazioneGestioneResponseDTO(
        UUID idSegnalazione,
        StatoSegnalazione stato,
        boolean teamSqualificato,
        String messaggio
) {
}
