package hackhub.dto;

import java.util.UUID;

/**
 * DTO immutabile di conferma dopo l'invio di una segnalazione di violazione.
 *
 * @param id        id della segnalazione appena creata
 * @param messaggio messaggio di conferma mostrato al mentore
 */
public record SegnalazioneResponseDTO(UUID id, String messaggio) {
}
