package hackhub.dto;

import java.util.UUID;

/**
 * DTO immutabile che rappresenta una singola sottomissione nella dashboard del giudice.
 * Il flag {@code valutato} consente alla vista di differenziare graficamente
 * le sottomissioni già valutate da quelle ancora in attesa.
 */
public record SottomissioneDaValutareItemDTO(
        UUID sottomissioneId,
        UUID teamId,
        String nomeTeam,
        String linkRepo,
        String linkDemo,
        String descrizione,
        boolean valutato
) {
}
