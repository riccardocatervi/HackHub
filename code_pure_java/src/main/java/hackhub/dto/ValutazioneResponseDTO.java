package hackhub.dto;

import java.util.UUID;

/**
 * DTO immutabile di conferma dopo il salvataggio di una valutazione.
 *
 * @param id              id della valutazione appena creata
 * @param sottomissioneId id della sottomissione valutata
 * @param nomeTeam        nome del team a cui appartiene la sottomissione
 * @param punteggio       voto assegnato
 * @param giudizioScritto giudizio scritto inserito dal giudice
 */
public record ValutazioneResponseDTO(
        UUID id,
        UUID sottomissioneId,
        String nomeTeam,
        double punteggio,
        String giudizioScritto
) {
}
