package hackhub.dto;

import java.util.List;
import java.util.UUID;

/**
 * DTO immutabile con la lista completa delle sottomissioni visibili nella dashboard del giudice.
 * Ogni item indica se la sottomissione è già stata valutata o meno,
 * permettendo alla vista di disabilitare le voci già gestite.
 */
public record SottomissioniDaValutareDTO(
        UUID hackathonId,
        String nomeHackathon,
        List<SottomissioneDaValutareItemDTO> sottomissioni
) {
}
