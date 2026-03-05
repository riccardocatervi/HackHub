package hackhub.dto;

import java.util.UUID;

/**
 * DTO immutabile che rappresenta un singolo team selezionabile nel modulo di segnalazione.
 */
public record TeamSegnalabileItemDTO(UUID teamId, String nomeTeam) {
}
