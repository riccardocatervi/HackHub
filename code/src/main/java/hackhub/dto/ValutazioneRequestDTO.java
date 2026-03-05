package hackhub.dto;

import java.util.UUID;

/**
 * DTO immutabile con i dati inseriti dal giudice per valutare una sottomissione.
 *
 * @param sottomissioneId  id della sottomissione da valutare
 * @param giudiceId        id del giudice che effettua la valutazione
 * @param hackathonId      id dell'hackathon di riferimento (per il controllo stato)
 * @param punteggio        voto assegnato, compreso tra 0.0 e 10.0 (non necessariamente intero)
 * @param giudizioScritto  motivazione scritta della valutazione
 */
public record ValutazioneRequestDTO(
        UUID sottomissioneId,
        UUID giudiceId,
        UUID hackathonId,
        double punteggio,
        String giudizioScritto
) {
}
